package ym_cosmetic.pick_perfume_be.common.event

data class VoteUpdatedEvent(
    val memberId: Long,
    val perfumeId: Long,
    val category: String,
    val value: String
)