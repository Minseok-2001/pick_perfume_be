package ym_cosmetic.pick_perfume_be.perfume.dto.request

import ym_cosmetic.pick_perfume_be.perfume.enums.Gender

data class PerfumeFilterRequest(
    val brandIds: List<Long>? = null,
    val genders: List<Gender>? = null,
    val accordIds: List<Long>? = null
) 