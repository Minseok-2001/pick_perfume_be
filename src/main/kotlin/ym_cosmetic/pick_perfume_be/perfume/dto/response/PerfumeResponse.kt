package ym_cosmetic.pick_perfume_be.perfume.dto.response

import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume
import ym_cosmetic.pick_perfume_be.perfume.vo.Concentration
import ym_cosmetic.pick_perfume_be.perfume.vo.NoteType
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
