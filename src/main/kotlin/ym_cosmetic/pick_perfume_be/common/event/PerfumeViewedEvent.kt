package ym_cosmetic.pick_perfume_be.common.event

data class PerfumeViewedEvent(
    val memberId: Long,
    val perfumeId: Long
)