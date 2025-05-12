package ym_cosmetic.pick_perfume_be.perfume.dto

import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume

/**
 * 향수 요약 정보 DTO
 */
data class PerfumeSummaryDto(
    val id: Long,
    val name: String,
    val brand: String,
    val imageUrl: String?,
    val averageRating: Double
) {
    companion object {
        fun from(perfume: Perfume): PerfumeSummaryDto {
            return PerfumeSummaryDto(
                id = perfume.id!!,
                name = perfume.name,
                brand = perfume.brand.name,
                imageUrl = perfume.imageUrl,
                averageRating = perfume.averageRating ?: 0.0
            )
        }
    }
} 