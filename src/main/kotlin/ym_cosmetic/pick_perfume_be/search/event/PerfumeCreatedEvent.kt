package ym_cosmetic.pick_perfume_be.search.event

data class PerfumeCreatedEvent(val perfumeId: Long)
data class PerfumeUpdatedEvent(val perfumeId: Long)
data class PerfumeDeletedEvent(val perfumeId: Long)