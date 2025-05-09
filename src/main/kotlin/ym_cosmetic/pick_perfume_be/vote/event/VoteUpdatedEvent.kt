package ym_cosmetic.pick_perfume_be.vote.event

import ym_cosmetic.pick_perfume_be.vote.vo.VoteCategory

data class VoteUpdatedEvent(
    val userId: Long,
    val perfumeId: Long,
    val category: VoteCategory,
    val value: String
)