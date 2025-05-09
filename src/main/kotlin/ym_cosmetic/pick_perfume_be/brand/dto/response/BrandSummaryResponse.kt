package ym_cosmetic.pick_perfume_be.brand.dto.response

import ym_cosmetic.pick_perfume_be.brand.entity.Brand

data class BrandSummaryResponse(
    val id: Long,
    val name: String,
    val logoUrl: String?,
    val country: String?,
    val isLuxury: Boolean,
    val isNiche: Boolean
) {
    companion object {
        fun from(brand: Brand): BrandSummaryResponse {
            return BrandSummaryResponse(
                id = brand.id!!,
                name = brand.name,
                logoUrl = brand.logo?.url,
                country = brand.country?.name,
                isLuxury = brand.isLuxury,
                isNiche = brand.isNiche
            )
        }
    }
}