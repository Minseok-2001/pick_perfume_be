package ym_cosmetic.pick_perfume_be.vote.dto.response

data class VoteValueStatistics(
    val value: String,
    val display: String,
    val count: Int,
    val percentage: Int
)