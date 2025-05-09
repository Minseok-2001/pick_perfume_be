package ym_cosmetic.pick_perfume_be.vote.event

data class VoteCreatedEvent(
    val userId: Long,
    val perfumeId: Long,
    val category: String,
    val value: String
)

