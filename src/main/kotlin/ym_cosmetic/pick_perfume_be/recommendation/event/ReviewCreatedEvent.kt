package ym_cosmetic.pick_perfume_be.recommendation.event

data class ReviewCreatedEvent(val userId: Long, val perfumeId: Long, val rating: Int)
