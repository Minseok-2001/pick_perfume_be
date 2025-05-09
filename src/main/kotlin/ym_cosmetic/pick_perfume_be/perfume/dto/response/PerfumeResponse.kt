package ym_cosmetic.pick_perfume_be.perfume.dto.response

import ym_cosmetic.pick_perfume_be.accord.dto.response.AccordResponse
import ym_cosmetic.pick_perfume_be.brand.dto.response.BrandSummaryResponse
import ym_cosmetic.pick_perfume_be.designer.dto.response.DesignerSummaryResponse
import ym_cosmetic.pick_perfume_be.note.dto.response.NoteResponse
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume
import ym_cosmetic.pick_perfume_be.perfume.enums.DesignerRole
import ym_cosmetic.pick_perfume_be.perfume.vo.Concentration
import ym_cosmetic.pick_perfume_be.perfume.vo.NoteType
import java.time.LocalDateTime

data class PerfumeResponse(
    val id: Long,
    val name: String,
    val brand: BrandSummaryResponse,
    val description: String?,
    val releaseYear: Int?,
    val concentration: Concentration?,
    val imageUrl: String?,
    val topNotes: List<NoteResponse>,
    val middleNotes: List<NoteResponse>,
    val baseNotes: List<NoteResponse>,
    val accords: List<AccordResponse>,
    val designers: List<PerfumeDesignerResponse>,
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
            val designers = perfume.designers

            return PerfumeResponse(
                id = perfume.id!!,
                name = perfume.name,
                brand = BrandSummaryResponse.from(perfume.brand),
                description = perfume.description,
                releaseYear = perfume.releaseYear,
                concentration = perfume.concentration,
                imageUrl = perfume.image?.url,
                topNotes = notes.filter { it.type == NoteType.TOP }
                    .map { NoteResponse.from(it.note) },
                middleNotes = notes.filter { it.type == NoteType.MIDDLE }
                    .map { NoteResponse.from(it.note) },
                baseNotes = notes.filter { it.type == NoteType.BASE }
                    .map { NoteResponse.from(it.note) },
                accords = perfume.getAccords().map {
                    AccordResponse.from(it.accord)
                },
                designers = designers.map {
                    PerfumeDesignerResponse(
                        designer = DesignerSummaryResponse.from(it.designer),
                        role = it.role,
                        description = it.description
                    )
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

data class PerfumeDesignerResponse(
    val designer: DesignerSummaryResponse,
    val role: DesignerRole,
    val description: String?
)