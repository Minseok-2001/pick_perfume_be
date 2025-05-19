package ym_cosmetic.pick_perfume_be.perfume.service

import org.springframework.context.ApplicationEventPublisher
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
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.member.enums.MemberRole
import ym_cosmetic.pick_perfume_be.member.repository.MemberRepository
import ym_cosmetic.pick_perfume_be.note.entity.Note
import ym_cosmetic.pick_perfume_be.note.repository.NoteRepository
import ym_cosmetic.pick_perfume_be.perfume.dto.request.PerfumeCreateRequest
import ym_cosmetic.pick_perfume_be.perfume.dto.request.PerfumeDesignerRequest
import ym_cosmetic.pick_perfume_be.perfume.dto.request.PerfumeUpdateRequest
import ym_cosmetic.pick_perfume_be.perfume.dto.request.PerfumeFilterRequest
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumeResponse
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumeSummaryResponse
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumePageResponse
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumeSummaryStats
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume
import ym_cosmetic.pick_perfume_be.perfume.entity.PerfumeLike
import ym_cosmetic.pick_perfume_be.perfume.repository.*
import ym_cosmetic.pick_perfume_be.perfume.vo.NoteType
import ym_cosmetic.pick_perfume_be.search.event.PerfumeCreatedEvent
import ym_cosmetic.pick_perfume_be.search.event.PerfumeDeletedEvent
import ym_cosmetic.pick_perfume_be.search.event.PerfumeUpdatedEvent

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
    private val s3Service: S3Service,
    private val eventPublisher: ApplicationEventPublisher,
    private val perfumeLikeRepository: PerfumeLikeRepository

) {
    @Transactional(readOnly = true)
    fun findPerfumeById(id: Long, member: Member?): PerfumeResponse {
        val perfume = perfumeRepository.findByIdWithCreatorAndBrand(id)
            ?: throw EntityNotFoundException("Perfume not found with id: $id")

        val isLiked = checkIfPerfumeLikedByMember(id, member)


        return PerfumeResponse.from(perfume, isLiked)
    }

    @Transactional(readOnly = true)
    fun findAllApprovedPerfumes(pageable: Pageable, member: Member?): Page<PerfumeSummaryResponse> {
        val perfumePage = perfumeRepository.findAllApprovedWithCreatorAndBrand(pageable)
        val likedPerfumeIds = getLikedPerfumeIdsByMember(member)

        return perfumePage.map { perfume ->
            PerfumeSummaryResponse.from(
                perfume = perfume,
                isLiked = likedPerfumeIds.contains(perfume.id)
            )
        }
    }

    @Transactional(readOnly = true)
    fun findAllPerfumesWithFilter(
        filter: PerfumeFilterRequest?, 
        pageable: Pageable, 
        member: Member?,
        includeStats: Boolean = false
    ): PerfumePageResponse {
        // 필터가 없는 경우 기본 조회
        val perfumePage = if (filter == null) {
            perfumeRepository.findAllApprovedWithCreatorAndBrand(pageable)
        } else {
            perfumeRepository.findAllApprovedWithFilter(filter, pageable)
        }
        
        val likedPerfumeIds = getLikedPerfumeIdsByMember(member)
        
        val perfumesPage = perfumePage.map { perfume ->
            PerfumeSummaryResponse.from(
                perfume = perfume,
                isLiked = likedPerfumeIds.contains(perfume.id)
            )
        }
        
        // 통계 정보 포함 여부에 따라 반환
        val stats = if (includeStats) {
            getPerfumeStatistics()
        } else {
            null
        }
        
        return PerfumePageResponse(
            perfumes = perfumesPage,
            stats = stats
        )
    }
    
    @Transactional(readOnly = true)
    fun getPerfumeStatistics(): PerfumeSummaryStats {
        val brandStats = perfumeRepository.findTopBrandStats(10)
        val genderStats = perfumeRepository.findGenderStats()
        val accordStats = perfumeRepository.findTopAccordStats(10)
        
        return PerfumeSummaryStats(
            brandStats = brandStats,
            genderStats = genderStats,
            accordStats = accordStats
        )
    }

    @Transactional
    fun createPerfume(request: PerfumeCreateRequest, memberId: Long?): PerfumeResponse {
        val creator = memberId?.let {
            memberRepository.findById(it).orElse(null)
        }

        // 브랜드 조회 또는 생성
        val brand = getBrandByNameOrCreate(request.brandName)

        val perfume = Perfume.create(
            name = request.name,
            brand = brand,
            gender = request.gender,
            content = request.content,
            releaseYear = request.releaseYear,
            concentration = request.concentration,
            image = request.imageUrl?.let { ImageUrl(it) },
            creator = creator,
            isAdmin = creator?.memberRole == MemberRole.ADMIN
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
        eventPublisher.publishEvent(PerfumeCreatedEvent(savedPerfume.id!!))

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
            content = request.content,
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
        eventPublisher.publishEvent(PerfumeUpdatedEvent(id))

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
        eventPublisher.publishEvent(PerfumeDeletedEvent(id))
    }

    private fun checkIfPerfumeLikedByMember(perfumeId: Long, member: Member?): Boolean {
        if (member == null || member.id == null) {
            return false
        }

        return perfumeLikeRepository.existsByPerfumeIdAndMemberId(perfumeId, member.id!!)
    }

    private fun getLikedPerfumeIdsByMember(member: Member?): Set<Long> {
        if (member == null || member.id == null) {
            return emptySet()
        }

        return perfumeLikeRepository.findPerfumeIdsByMemberId(member.id!!)
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

            val perfumeNote = perfume.addNote(note, type)
            perfumeNoteRepository.save(perfumeNote)
        }
    }

    private fun addPerfumeAccords(perfume: Perfume, accordNames: List<String>) {
        accordNames.forEach { accordName ->
            val accord = accordRepository.findByNameIgnoreCase(accordName)
                ?: accordRepository.save(Accord(name = accordName))

            val perfumeAccord = perfume.addAccord(accord)
            perfumeAccordRepository.save(perfumeAccord)
        }
    }

    private fun addPerfumeDesigners(perfume: Perfume, designers: List<PerfumeDesignerRequest>) {
        designers.forEach { designerRequest ->
            val designer = getDesignerByNameOrCreate(designerRequest.designerName)
            val perfumeDesigner =
                perfume.addDesigner(designer, designerRequest.role, designerRequest.content)
            perfumeDesignerRepository.save(perfumeDesigner)
        }
    }

    private fun updatePerfumeNotes(perfume: Perfume, noteNames: List<String>, type: NoteType) {
        // 기존 노트 삭제
        perfumeNoteRepository.deleteByPerfumeIdAndType(perfume.id!!, type)

        // 새 노트 추가
        addPerfumeNotes(perfume, noteNames, type)
    }

    private fun updatePerfumeAccords(perfume: Perfume, accordNames: List<String>) {
        // 기존 어코드 삭제
        perfumeAccordRepository.deleteByPerfumeId(perfume.id!!)

        // 새 어코드 추가
        addPerfumeAccords(perfume, accordNames)
    }

    private fun updatePerfumeDesigners(perfume: Perfume, designers: List<PerfumeDesignerRequest>) {
        // 기존 디자이너 삭제
        perfumeDesignerRepository.deleteByPerfumeId(perfume.id!!)

        // 새 디자이너 추가
        addPerfumeDesigners(perfume, designers)
    }

    @Transactional
    fun likePerfume(perfumeId: Long, member: Member): Boolean {
        if (member.id == null) {
            throw IllegalArgumentException("인증된 사용자만 좋아요를 할 수 있습니다.")
        }

        val perfume = perfumeRepository.findById(perfumeId)
            .orElseThrow { EntityNotFoundException("해당 향수를 찾을 수 없습니다: $perfumeId") }
            
        if (perfumeLikeRepository.existsByPerfumeIdAndMemberId(perfumeId, member.id!!)) {
            return true // 이미 좋아요 상태
        }
        
        val perfumeLike = PerfumeLike.create(perfume, member)
        perfumeLikeRepository.save(perfumeLike)
        
        return true
    }
    
    @Transactional
    fun unlikePerfume(perfumeId: Long, member: Member): Boolean {
        if (member.id == null) {
            throw IllegalArgumentException("인증된 사용자만 좋아요 취소를 할 수 있습니다.")
        }
        
        perfumeLikeRepository.findByPerfumeIdAndMemberId(perfumeId, member.id!!)?.let {
            perfumeLikeRepository.delete(it)
        }
        
        return false
    }
}