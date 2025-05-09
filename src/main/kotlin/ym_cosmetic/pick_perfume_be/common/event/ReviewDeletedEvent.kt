package ym_cosmetic.pick_perfume_be.common.event

data class ReviewDeletedEvent(
    val memberId: Long,
    val perfumeId: Long
)
