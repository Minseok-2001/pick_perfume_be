package ym_cosmetic.pick_perfume_be.perfume.dto.response

import ym_cosmetic.pick_perfume_be.brand.dto.response.BrandSummaryResponse
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume
import ym_cosmetic.pick_perfume_be.perfume.vo.Concentration

data class PerfumeSummaryResponse(
    val id: Long,
    val name: String,
    val brand: BrandSummaryResponse,
    val releaseYear: Int?,
    val concentration: Concentration?,
    val imageUrl: String?,
    val averageRating: Double,
    val reviewCount: Int,
    val topAccords: List<String>,
    val primaryPerfumer: String?
) {
    companion object {
        fun from(perfume: Perfume): PerfumeSummaryResponse {
            val primaryPerfumer = perfume.getPrimaryPerfumer()?.name

            return PerfumeSummaryResponse(
                id = perfume.id!!,
                name = perfume.name,
                brand = BrandSummaryResponse.from(perfume.brand),
                releaseYear = perfume.releaseYear,
                concentration = perfume.concentration,
                imageUrl = perfume.image?.url,
                averageRating = perfume.calculateAverageRating(),
                reviewCount = perfume.getReviewCount(),
                topAccords = perfume.getAccords()
                    .map { it.accord.name }
                    .take(3),
                primaryPerfumer = primaryPerfumer
            )
        }
    }
}