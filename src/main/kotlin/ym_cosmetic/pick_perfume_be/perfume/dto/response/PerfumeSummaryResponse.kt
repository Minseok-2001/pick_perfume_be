package ym_cosmetic.pick_perfume_be.perfume.dto.response

import ym_cosmetic.pick_perfume_be.brand.dto.response.BrandSummaryResponse
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume
import ym_cosmetic.pick_perfume_be.perfume.vo.Concentration
import java.time.LocalDateTime

data class PerfumeSummaryResponse(
    val id: Long,
    val name: String,
    val brand: BrandSummaryResponse,
    val releaseYear: Int?,
    val concentration: Concentration?,
    val imageUrl: String?,
    val averageRating: Double,
    val reviewCount: Int,
    val isLiked: Boolean = false,
    val creatorNickname: String?,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(perfume: Perfume, isLiked: Boolean): PerfumeSummaryResponse {
            return PerfumeSummaryResponse(
                id = perfume.id!!,
                name = perfume.name,
                brand = BrandSummaryResponse.from(perfume.brand),
                releaseYear = perfume.releaseYear,
                concentration = perfume.concentration,
                imageUrl = perfume.image?.url,
                isLiked = isLiked,
                averageRating = perfume.calculateAverageRating(),
                reviewCount = perfume.getReviewCount(),
                creatorNickname = perfume.creator?.nickname,
                createdAt = perfume.createdAt
            )
        }
    }
}