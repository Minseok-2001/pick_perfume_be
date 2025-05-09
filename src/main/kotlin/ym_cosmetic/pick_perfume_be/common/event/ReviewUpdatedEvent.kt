package ym_cosmetic.pick_perfume_be.common.event

data class ReviewUpdatedEvent(
    val memberId: Long,
    val perfumeId: Long,
    val rating: Int
)