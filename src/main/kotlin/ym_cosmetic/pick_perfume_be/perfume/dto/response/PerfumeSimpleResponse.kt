package ym_cosmetic.pick_perfume_be.perfume.dto.response

import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume

data class PerfumeSimpleResponse(
    val id: Long,
    val name: String,
    val brandName: String,
    val imageUrl: String?
) {
    companion object {
        fun from(perfume: Perfume): PerfumeSimpleResponse {
            return PerfumeSimpleResponse(
                id = perfume.id!!,
                name = perfume.name,
                brandName = perfume.brand.name,
                imageUrl = perfume.image?.url
            )
        }
    }
} 