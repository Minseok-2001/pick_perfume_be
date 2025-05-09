package ym_cosmetic.pick_perfume_be.perfume.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import ym_cosmetic.pick_perfume_be.accord.entity.Accord
import ym_cosmetic.pick_perfume_be.accord.repository.AccordRepository
import ym_cosmetic.pick_perfume_be.brand.entity.Brand
import ym_cosmetic.pick_perfume_be.brand.repository.BrandRepository
import ym_cosmetic.pick_perfume_be.common.exception.EntityNotFoundException
import ym_cosmetic.pick_perfume_be.common.vo.ImageUrl
import ym_cosmetic.pick_perfume_be.designer.entity.Designer
import ym_cosmetic.pick_perfume_be.designer.repository.DesignerRepository
import ym_cosmetic.pick_perfume_be.infrastructure.s3.S3Service
import ym_cosmetic.pick_perfume_be.member.enums.MemberRole
import ym_cosmetic.pick_perfume_be.member.repository.MemberRepository
import ym_cosmetic.pick_perfume_be.note.entity.Note
import ym_cosmetic.pick_perfume_be.note.repository.NoteRepository
import ym_cosmetic.pick_perfume_be.perfume.dto.request.PerfumeCreateRequest
import ym_cosmetic.pick_perfume_be.perfume.dto.request.PerfumeDesignerRequest
import ym_cosmetic.pick_perfume_be.perfume.dto.request.PerfumeUpdateRequest
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumeResponse
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumeSummaryResponse
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume
import ym_cosmetic.pick_perfume_be.perfume.entity.PerfumeAccord
import ym_cosmetic.pick_perfume_be.perfume.entity.PerfumeDesigner
import ym_cosmetic.pick_perfume_be.perfume.entity.PerfumeNote
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeAccordRepository
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeDesignerRepository
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeNoteRepository
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeRepository
import ym_cosmetic.pick_perfume_be.perfume.vo.NoteType

@Service
class PerfumeService(
    private val perfumeRepository: PerfumeRepository,
    private val noteRepository: NoteRepository,
    private val accordRepository: AccordRepository,
    private val brandRepository: BrandRepository,
    private val designerRepository: DesignerRepository,
    private val perfumeNoteRepository: PerfumeNoteRepository,
    private val perfumeAccordRepository: PerfumeAccordRepository,
    private val perfumeDesignerRepository: PerfumeDesignerRepository,
    private val memberRepository: MemberRepository,
    private val s3Service: S3Service
) {
    @Transactional(readOnly = true)
    fun findPerfumeById(id: Long): PerfumeResponse {
        val perfume = perfumeRepository.findByIdWithCreatorAndBrand(id)
            ?: throw EntityNotFoundException("Perfume not found with id: $id")

        return PerfumeResponse.from(perfume)
    }

    @Transactional(readOnly = true)
    fun findAllPerfumes(pageable: Pageable): Page<PerfumeSummaryResponse> {
        return perfumeRepository.findAllApprovedWithCreatorAndBrand(pageable)
            .map { PerfumeSummaryResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun searchPerfumes(query: String, pageable: Pageable): Page<PerfumeSummaryResponse> {
        return perfumeRepository.findByNameContainingOrBrandNameContaining(query, query, pageable)
            .map { PerfumeSummaryResponse.from(it) }
    }

    @Transactional
    fun createPerfume(request: PerfumeCreateRequest, memberId: Long?): PerfumeResponse {
        val creator = memberId?.let {
            memberRepository.findById(it).orElse(null)
        }

        // 브랜드 조회 또는 생성
        val brand = getBrandByNameOrCreate(request.brandName)

        val perfume = Perfume(
            name = request.name,
            brand = brand,  // 브랜드 엔티티 설정
            description = request.description,
            releaseYear = request.releaseYear,
            concentration = request.concentration,
            image = request.imageUrl?.let { ImageUrl(it) },
            creator = creator,
            isApproved = creator?.role == MemberRole.ADMIN
        )

        val savedPerfume = perfumeRepository.save(perfume)

        // 노트 추가
        addPerfumeNotes(savedPerfume, request.topNotes, NoteType.TOP)
        addPerfumeNotes(savedPerfume, request.middleNotes, NoteType.MIDDLE)
        addPerfumeNotes(savedPerfume, request.baseNotes, NoteType.BASE)

        // 어코드 추가
        addPerfumeAccords(savedPerfume, request.accords)

        // 디자이너 추가
        addPerfumeDesigners(savedPerfume, request.designers)

        return PerfumeResponse.from(savedPerfume)
    }

    @Transactional
    fun updatePerfume(id: Long, request: PerfumeUpdateRequest): PerfumeResponse {
        val perfume = perfumeRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Perfume not found with id: $id") }

        // 브랜드 업데이트
        val brand = getBrandByNameOrCreate(request.brandName)

        perfume.updateDetails(
            name = request.name,
            brand = brand,
            description = request.description,
            releaseYear = request.releaseYear,
            concentration = request.concentration
        )

        request.imageUrl?.let {
            perfume.updateImage(ImageUrl(it))
        }

        // 노트 업데이트
        updatePerfumeNotes(perfume, request.topNotes, NoteType.TOP)
        updatePerfumeNotes(perfume, request.middleNotes, NoteType.MIDDLE)
        updatePerfumeNotes(perfume, request.baseNotes, NoteType.BASE)

        // 어코드 업데이트
        updatePerfumeAccords(perfume, request.accords)

        // 디자이너 업데이트
        updatePerfumeDesigners(perfume, request.designers)

        return PerfumeResponse.from(perfume)
    }

    @Transactional
    fun uploadPerfumeImage(id: Long, file: MultipartFile): String {
        val perfume = perfumeRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Perfume not found with id: $id") }

        val imageUrl = s3Service.uploadFile("perfumes/${id}", file)
        perfume.updateImage(ImageUrl(imageUrl))

        return imageUrl
    }

    @Transactional
    fun approvePerfume(id: Long): PerfumeResponse {
        val perfume = perfumeRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Perfume not found with id: $id") }

        perfume.approve()

        return PerfumeResponse.from(perfume)
    }

    @Transactional
    fun deletePerfume(id: Long) {
        // 관련 데이터 삭제
        perfumeNoteRepository.deleteByPerfumeId(id)
        perfumeAccordRepository.deleteByPerfumeId(id)
        perfumeDesignerRepository.deleteByPerfumeId(id)

        // 향수 삭제
        perfumeRepository.deleteById(id)
    }

    private fun getBrandByNameOrCreate(brandName: String): Brand {
        return brandRepository.findByNameIgnoreCase(brandName)
            ?: brandRepository.save(Brand(name = brandName))
    }

    private fun getDesignerByNameOrCreate(designerName: String): Designer {
        return designerRepository.findByNameIgnoreCase(designerName)
            ?: designerRepository.save(Designer(name = designerName))
    }

    private fun addPerfumeNotes(perfume: Perfume, noteNames: List<String>, type: NoteType) {
        noteNames.forEach { noteName ->
            val note = noteRepository.findByNameIgnoreCase(noteName)
                ?: noteRepository.save(Note(name = noteName))

            val perfumeNote = PerfumeNote(
                perfume = perfume,
                note = note,
                type = type
            )
            perfumeNoteRepository.save(perfumeNote)
        }
    }

    private fun updatePerfumeNotes(perfume: Perfume, noteNames: List<String>, type: NoteType) {
        perfumeNoteRepository.deleteByPerfumeIdAndType(perfume.id!!, type)
        addPerfumeNotes(perfume, noteNames, type)
    }

    private fun addPerfumeAccords(perfume: Perfume, accordNames: List<String>) {
        accordNames.forEach { accordName ->
            val accord = accordRepository.findByNameIgnoreCase(accordName)
                ?: accordRepository.save(Accord(name = accordName))

            val perfumeAccord = PerfumeAccord(
                perfume = perfume,
                accord = accord
            )
            perfumeAccordRepository.save(perfumeAccord)
        }
    }

    private fun updatePerfumeAccords(perfume: Perfume, accordNames: List<String>) {
        perfumeAccordRepository.deleteByPerfumeId(perfume.id!!)
        addPerfumeAccords(perfume, accordNames)
    }

    private fun addPerfumeDesigners(
        perfume: Perfume,
        designerRequests: List<PerfumeDesignerRequest>
    ) {
        designerRequests.forEach { request ->
            val designer = getDesignerByNameOrCreate(request.designerName)

            val perfumeDesigner = PerfumeDesigner(
                perfume = perfume,
                designer = designer,
                role = request.role,
                description = request.description
            )
            perfumeDesignerRepository.save(perfumeDesigner)
        }
    }

    private fun updatePerfumeDesigners(
        perfume: Perfume,
        designerRequests: List<PerfumeDesignerRequest>
    ) {
        perfumeDesignerRepository.deleteByPerfumeId(perfume.id!!)

        addPerfumeDesigners(perfume, designerRequests)
    }
}