package ym_cosmetic.pick_perfume_be.recommendation.event

data class RecommendationClickedEvent(
    val memberId: Long,
    val perfumeId: Long,
    val recommendationType: String
)