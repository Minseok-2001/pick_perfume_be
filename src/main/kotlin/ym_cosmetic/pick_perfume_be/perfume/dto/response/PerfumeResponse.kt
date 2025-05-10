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
            
            // 디자이너 처리
            val designers = mutableListOf<PerfumeDesignerResponse>()
            
            // 각 역할별로 디자이너를 가져와서 처리
            DesignerRole.entries.forEach { role ->
                val roleDesigners = perfume.getDesignersByRole(role)
                roleDesigners.forEach { designer ->
                    designers.add(
                        PerfumeDesignerResponse(
                            designer = DesignerSummaryResponse.from(designer),
                            role = role,
                            description = null // 설명은 엔티티에서 직접 가져올 수 없어 null로 설정
                        )
                    )
                }
            }

            return PerfumeResponse(
                id = perfume.id!!,
                name = perfume.getName(),
                brand = BrandSummaryResponse.from(perfume.getBrand()),
                description = perfume.getDescription(),
                releaseYear = perfume.getReleaseYear(),
                concentration = perfume.getConcentration(),
                imageUrl = perfume.getImage()?.url,
                topNotes = notes.filter { it.getType() == NoteType.TOP }
                    .map { NoteResponse.from(it.getNote()) },
                middleNotes = notes.filter { it.getType() == NoteType.MIDDLE }
                    .map { NoteResponse.from(it.getNote()) },
                baseNotes = notes.filter { it.getType() == NoteType.BASE }
                    .map { NoteResponse.from(it.getNote()) },
                accords = perfume.getAccords().map {
                    AccordResponse.from(it.getAccord())
                },
                designers = designers,
                averageRating = perfume.calculateAverageRating(),
                reviewCount = perfume.getReviewCount(),
                creatorNickname = perfume.getCreator()?.nickname,
                isApproved = perfume.isApproved(),
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