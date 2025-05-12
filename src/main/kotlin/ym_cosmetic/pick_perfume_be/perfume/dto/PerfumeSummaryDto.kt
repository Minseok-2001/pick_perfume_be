package ym_cosmetic.pick_perfume_be.perfume.dto

import ym_cosmetic.pick_perfume_be.common.vo.ImageUrl
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume

/**
 * 향수 요약 정보 DTO
 */
data class PerfumeSummaryDto(
    val id: Long,
    val name: String,
    val brand: String,
    val imageUrl: ImageUrl?,
) {
    companion object {
        fun from(perfume: Perfume): PerfumeSummaryDto {
            return PerfumeSummaryDto(
                id = perfume.id!!,
                name = perfume.name,
                brand = perfume.brand.name,
                imageUrl = perfume.image,
            )
        }
    }
} 