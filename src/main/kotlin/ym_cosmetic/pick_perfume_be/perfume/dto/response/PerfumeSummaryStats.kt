package ym_cosmetic.pick_perfume_be.perfume.dto.response

import ym_cosmetic.pick_perfume_be.perfume.enums.Gender

data class PerfumeSummaryStats(
    val brandStats: List<BrandStat>,
    val genderStats: List<GenderStat>,
    val accordStats: List<AccordStat>
)

data class BrandStat(
    val brandId: Long,
    val brandName: String,
    val count: Long
)

data class GenderStat(
    val gender: Gender,
    val count: Long
)

data class AccordStat(
    val accordId: Long,
    val accordName: String,
    val count: Long
) 