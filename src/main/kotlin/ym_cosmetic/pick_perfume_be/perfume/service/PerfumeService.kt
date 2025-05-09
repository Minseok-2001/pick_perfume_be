package ym_cosmetic.pick_perfume_be.perfume.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import ym_cosmetic.pick_perfume_be.accord.entity.Accord
import ym_cosmetic.pick_perfume_be.accord.repository.AccordRepository
import ym_cosmetic.pick_perfume_be.common.exception.EntityNotFoundException
import ym_cosmetic.pick_perfume_be.common.vo.ImageUrl
import ym_cosmetic.pick_perfume_be.infrastructure.s3.S3Service
import ym_cosmetic.pick_perfume_be.member.enums.MemberRole
import ym_cosmetic.pick_perfume_be.member.repository.MemberRepository
import ym_cosmetic.pick_perfume_be.note.entity.Note
import ym_cosmetic.pick_perfume_be.note.repository.NoteRepository
import ym_cosmetic.pick_perfume_be.perfume.dto.request.PerfumeCreateRequest
import ym_cosmetic.pick_perfume_be.perfume.dto.request.PerfumeUpdateRequest
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumeResponse
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumeSummaryResponse
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume
import ym_cosmetic.pick_perfume_be.perfume.entity.PerfumeAccord
import ym_cosmetic.pick_perfume_be.perfume.entity.PerfumeNote
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeAccordRepository
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeNoteRepository
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeRepository
import ym_cosmetic.pick_perfume_be.perfume.vo.NoteType

@Service
class PerfumeService(
    private val perfumeRepository: PerfumeRepository,
    private val noteRepository: NoteRepository,
    private val accordRepository: AccordRepository,
    private val perfumeNoteRepository: PerfumeNoteRepository,
    private val perfumeAccordRepository: PerfumeAccordRepository,
    private val memberRepository: MemberRepository,
    private val s3Service: S3Service
) {
    @Transactional(readOnly = true)
    fun findPerfumeById(id: Long): PerfumeResponse {
        val perfume = perfumeRepository.findByIdWithCreator(id)
            ?: throw EntityNotFoundException("Perfume not found with id: $id")

        return PerfumeResponse.from(perfume)
    }

    @Transactional(readOnly = true)
    fun findAllPerfumes(pageable: Pageable): Page<PerfumeSummaryResponse> {
        return perfumeRepository.findAllApprovedWithCreator(pageable)
            .map { PerfumeSummaryResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun searchPerfumes(query: String, pageable: Pageable): Page<PerfumeSummaryResponse> {
        return perfumeRepository.findByNameContainingOrBrandContaining(query, query, pageable)
            .map { PerfumeSummaryResponse.from(it) }
    }

    @Transactional
    fun createPerfume(request: PerfumeCreateRequest, memberId: Long?): PerfumeResponse {
        val creator = memberId?.let {
            memberRepository.findById(it).orElse(null)
        }

        val perfume = Perfume(
            name = request.name,
            brand = request.brand,
            description = request.description,
            releaseYear = request.releaseYear,
            perfumer = request.perfumer,
            concentration = request.concentration,
            image = request.imageUrl?.let { ImageUrl(it) },
            creator = creator,
            isApproved = creator?.role == MemberRole.ADMIN
        )

        val savedPerfume = perfumeRepository.save(perfume)

        addPerfumeNotes(savedPerfume, request.topNotes, NoteType.TOP)
        addPerfumeNotes(savedPerfume, request.middleNotes, NoteType.MIDDLE)
        addPerfumeNotes(savedPerfume, request.baseNotes, NoteType.BASE)

        addPerfumeAccords(savedPerfume, request.accords)

        return PerfumeResponse.from(savedPerfume)
    }

    @Transactional
    fun updatePerfume(id: Long, request: PerfumeUpdateRequest): PerfumeResponse {
        val perfume = perfumeRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Perfume not found with id: $id") }

        perfume.updateDetails(
            name = request.name,
            brand = request.brand,
            description = request.description,
            releaseYear = request.releaseYear,
            perfumer = request.perfumer,
            concentration = request.concentration
        )

        request.imageUrl?.let {
            perfume.updateImage(ImageUrl(it))
        }

        updatePerfumeNotes(perfume, request.topNotes, NoteType.TOP)
        updatePerfumeNotes(perfume, request.middleNotes, NoteType.MIDDLE)
        updatePerfumeNotes(perfume, request.baseNotes, NoteType.BASE)

        updatePerfumeAccords(perfume, request.accords)

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
        perfumeNoteRepository.deleteByPerfumeId(id)
        perfumeAccordRepository.deleteByPerfumeId(id)

        perfumeRepository.deleteById(id)
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
}