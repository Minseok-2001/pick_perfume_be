package ym_cosmetic.pick_perfume_be.common.event

data class VoteCreatedEvent(
    val memberId: Long,
    val perfumeId: Long,
    val category: String,
    val value: String
)
