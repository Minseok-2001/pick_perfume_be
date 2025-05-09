package ym_cosmetic.pick_perfume_be.perfume.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ym_cosmetic.pick_perfume_be.accord.entity.Accord
import ym_cosmetic.pick_perfume_be.accord.repository.AccordRepository
import ym_cosmetic.pick_perfume_be.common.exception.EntityNotFoundException
import ym_cosmetic.pick_perfume_be.common.vo.ImageUrl
import ym_cosmetic.pick_perfume_be.infrastructure.s3.S3Service
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.member.enums.UserRole
import ym_cosmetic.pick_perfume_be.member.repository.MemberRepository
import ym_cosmetic.pick_perfume_be.note.repository.NoteRepository
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume
import ym_cosmetic.pick_perfume_be.perfume.entity.PerfumeAccord
import ym_cosmetic.pick_perfume_be.perfume.entity.PerfumeNote
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeAccordRepository
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeNoteRepository
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeRepository
import ym_cosmetic.pick_perfume_be.perfume.vo.NoteType

네, Perfume 도메인의 CRUD 기능을 위한 DTOs, Controller, Service 코드를 작성해 드리겠습니다. VO를 활용하여 응집도 높은 코드를 구현하겠습니다.
1. Perfume DTO (Request/Response)
먼저 요청과 응답을 위한 DTO들을 정의합니다.
kotlin// domain/perfume/dto/request/PerfumeCreateRequest.kt
package com.fragrantica.domain.perfume.dto.request

import com.fragrantica.domain.perfume.vo.Concentration

data class PerfumeCreateRequest(
    val name: String,
    val brand: String,
    val description: String?,
    val releaseYear: Int?,
    val perfumer: String?,
    val concentration: Concentration?,
    val imageUrl: String?,
    val topNotes: List<String> = emptyList(),
    val middleNotes: List<String> = emptyList(),
    val baseNotes: List<String> = emptyList(),
    val accords: List<String> = emptyList()
)
kotlin// domain/perfume/dto/request/PerfumeUpdateRequest.kt
package com.fragrantica.domain.perfume.dto.request

import com.fragrantica.domain.perfume.vo.Concentration

data class PerfumeUpdateRequest(
    val name: String,
    val brand: String,
    val description: String?,
    val releaseYear: Int?,
    val perfumer: String?,
    val concentration: Concentration?,
    val imageUrl: String?,
    val topNotes: List<String> = emptyList(),
    val middleNotes: List<String> = emptyList(),
    val baseNotes: List<String> = emptyList(),
    val accords: List<String> = emptyList()
)
kotlin// domain/perfume/dto/response/PerfumeResponse.kt
package com.fragrantica.domain.perfume.dto.response

import com.fragrantica.domain.common.vo.ImageUrl
import com.fragrantica.domain.perfume.entity.Perfume
import com.fragrantica.domain.perfume.vo.Concentration
import com.fragrantica.domain.perfume.vo.NoteType
import java.time.LocalDateTime

data class PerfumeResponse(
    val id: Long,
    val name: String,
    val brand: String,
    val description: String?,
    val releaseYear: Int?,
    val perfumer: String?,
    val concentration: Concentration?,
    val imageUrl: String?,
    val topNotes: List<NoteResponse>,
    val middleNotes: List<NoteResponse>,
    val baseNotes: List<NoteResponse>,
    val accords: List<AccordResponse>,
    val averageRating: Double,
    val reviewCount: Int,
    val creatorNickname: String?,
    val isApproved: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(perfume: Perfume): PerfumeResponse {
            val notes = perfume.getNotes()

            return PerfumeResponse(
                id = perfume.id!!,
                name = perfume.name,
                brand = perfume.brand,
                description = perfume.description,
                releaseYear = perfume.releaseYear,
                perfumer = perfume.perfumer,
                concentration = perfume.concentration,
                imageUrl = perfume.image?.url,
                topNotes = notes.filter { it.type == NoteType.TOP }
                    .map { NoteResponse(it.note.id!!, it.note.name, it.note.image?.url) },
                middleNotes = notes.filter { it.type == NoteType.MIDDLE }
                    .map { NoteResponse(it.note.id!!, it.note.name, it.note.image?.url) },
                baseNotes = notes.filter { it.type == NoteType.BASE }
                    .map { NoteResponse(it.note.id!!, it.note.name, it.note.image?.url) },
                accords = perfume.getAccords().map {
                    AccordResponse(it.accord.id!!, it.accord.name, it.accord.color)
                },
                averageRating = perfume.calculateAverageRating(),
                reviewCount = perfume.getReviewCount(),
                creatorNickname = perfume.creator?.nickname,
                isApproved = perfume.isApproved,
                createdAt = perfume.createdAt,
                updatedAt = perfume.updatedAt
            )
        }
    }
}

data class NoteResponse(
    val id: Long,
    val name: String,
    val imageUrl: String?
)

data class AccordResponse(
    val id: Long,
    val name: String,
    val color: String?
)
kotlin// domain/perfume/dto/response/PerfumeSummaryResponse.kt
package com.fragrantica.domain.perfume.dto.response

import com.fragrantica.domain.perfume.entity.Perfume
import com.fragrantica.domain.perfume.vo.Concentration

data class PerfumeSummaryResponse(
    val id: Long,
    val name: String,
    val brand: String,
    val releaseYear: Int?,
    val concentration: Concentration?,
    val imageUrl: String?,
    val averageRating: Double,
    val reviewCount: Int,
    val topAccords: List<String>
) {
    companion object {
        fun from(perfume: Perfume): PerfumeSummaryResponse {
            return PerfumeSummaryResponse(
                id = perfume.id!!,
                name = perfume.name,
                brand = perfume.brand,
                releaseYear = perfume.releaseYear,
                concentration = perfume.concentration,
                imageUrl = perfume.image?.url,
                averageRating = perfume.calculateAverageRating(),
                reviewCount = perfume.getReviewCount(),
                topAccords = perfume.getAccords()
                    .map { it.accord.name }
                    .take(3)
            )
        }
    }
}
2. Perfume Repository 인터페이스 및 추가 Repository
kotlin// domain/perfume/repository/PerfumeRepository.kt
package com.fragrantica.domain.perfume.repository

import com.fragrantica.domain.perfume.entity.Perfume
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PerfumeRepository : JpaRepository<Perfume, Long> {
    fun findByNameContainingOrBrandContaining(name: String, brand: String, pageable: Pageable): Page<Perfume>

    fun findByBrand(brand: String, pageable: Pageable): Page<Perfume>

    @Query("""
        SELECT DISTINCT p FROM Perfume p
        LEFT JOIN FETCH p.creator
        WHERE p.id = :id
    """)
    fun findByIdWithCreator(id: Long): Perfume?

    @Query("""
        SELECT DISTINCT p FROM Perfume p
        LEFT JOIN FETCH p.creator
        WHERE p.isApproved = true
    """)
    fun findAllApprovedWithCreator(pageable: Pageable): Page<Perfume>

    @Query("""
        SELECT p FROM Perfume p
        WHERE p.isApproved = true
        ORDER BY p.createdAt DESC
    """)
    fun findRecentlyAdded(pageable: Pageable): Page<Perfume>
}
kotlin// domain/perfume/repository/PerfumeNoteRepository.kt
package com.fragrantica.domain.perfume.repository

import com.fragrantica.domain.perfume.entity.PerfumeNote
import com.fragrantica.domain.perfume.vo.NoteType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface PerfumeNoteRepository : JpaRepository<PerfumeNote, Long> {
    fun findByPerfumeId(perfumeId: Long): List<PerfumeNote>

    fun findByPerfumeIdAndType(perfumeId: Long, type: NoteType): List<PerfumeNote>

    @Modifying
    @Query("DELETE FROM PerfumeNote pn WHERE pn.perfume.id = :perfumeId")
    fun deleteByPerfumeId(perfumeId: Long)

    @Modifying
    @Query("DELETE FROM PerfumeNote pn WHERE pn.perfume.id = :perfumeId AND pn.type = :type")
    fun deleteByPerfumeIdAndType(perfumeId: Long, type: NoteType)
}
kotlin// domain/perfume/repository/PerfumeAccordRepository.kt
package com.fragrantica.domain.perfume.repository

import com.fragrantica.domain.perfume.entity.PerfumeAccord
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface PerfumeAccordRepository : JpaRepository<PerfumeAccord, Long> {
    fun findByPerfumeId(perfumeId: Long): List<PerfumeAccord>

    @Modifying
    @Query("DELETE FROM PerfumeAccord pa WHERE pa.perfume.id = :perfumeId")
    fun deleteByPerfumeId(perfumeId: Long)
}
3. Perfume Service
kotlin// domain/perfume/service/PerfumeService.kt
package com.fragrantica.domain.perfume.service

import com.fragrantica.domain.accord.entity.Accord
import com.fragrantica.domain.accord.repository.AccordRepository
import com.fragrantica.domain.common.exception.EntityNotFoundException
import com.fragrantica.domain.common.vo.ImageUrl
import com.fragrantica.domain.member.entity.Member
import com.fragrantica.domain.member.repository.MemberRepository
import com.fragrantica.domain.note.entity.Note
import com.fragrantica.domain.note.repository.NoteRepository
import com.fragrantica.domain.perfume.dto.request.PerfumeCreateRequest
import com.fragrantica.domain.perfume.dto.request.PerfumeUpdateRequest
import com.fragrantica.domain.perfume.dto.response.PerfumeResponse
import com.fragrantica.domain.perfume.dto.response.PerfumeSummaryResponse
import com.fragrantica.domain.perfume.entity.Perfume
import com.fragrantica.domain.perfume.entity.PerfumeAccord
import com.fragrantica.domain.perfume.entity.PerfumeNote
import com.fragrantica.domain.perfume.repository.PerfumeAccordRepository
import com.fragrantica.domain.perfume.repository.PerfumeNoteRepository
import com.fragrantica.domain.perfume.repository.PerfumeRepository
import com.fragrantica.domain.perfume.vo.NoteType
import com.fragrantica.infrastructure.s3.S3Service
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

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
            isApproved = creator?.role == UserRole.ADMIN // 관리자가 생성하면 자동 승인
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

        // 노트 업데이트 (기존 노트 삭제 후 새로 추가)
        updatePerfumeNotes(perfume, request.topNotes, NoteType.TOP)
        updatePerfumeNotes(perfume, request.middleNotes, NoteType.MIDDLE)
        updatePerfumeNotes(perfume, request.baseNotes, NoteType.BASE)

        // 어코드 업데이트 (기존 어코드 삭제 후 새로 추가)
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
        // 관련 데이터 삭제
        perfumeNoteRepository.deleteByPerfumeId(id)
        perfumeAccordRepository.deleteByPerfumeId(id)

        // 기타 관련 데이터(리뷰, 투표 등) 삭제 로직 필요

        // 향수 삭제
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
        // 해당 타입의 기존 노트 삭제
        perfumeNoteRepository.deleteByPerfumeIdAndType(perfume.id!!, type)

        // 새 노트 추가
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
        // 기존 어코드 삭제
        perfumeAccordRepository.deleteByPerfumeId(perfume.id!!)

        // 새 어코드 추가
        addPerfumeAccords(perfume, accordNames)
    }
}
