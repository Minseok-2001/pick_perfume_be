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
    val creatorNickname: String?,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(perfume: Perfume): PerfumeSummaryResponse {
            return PerfumeSummaryResponse(
                id = perfume.id!!,
                name = perfume.getName(),
                brand = BrandSummaryResponse.from(perfume.getBrand()),
                releaseYear = perfume.getReleaseYear(),
                concentration = perfume.getConcentration(),
                imageUrl = perfume.getImage()?.url,
                averageRating = perfume.calculateAverageRating(),
                reviewCount = perfume.getReviewCount(),
                creatorNickname = perfume.getCreator()?.nickname,
                createdAt = perfume.createdAt
            )
        }
    }
}