package ym_cosmetic.pick_perfume_be.recommendation.dto.request

import ym_cosmetic.pick_perfume_be.recommendation.enums.FeedbackType

data class SurveyRecommendationFeedbackRequest(
    val perfumeId: Long,
    val feedbackType: FeedbackType,
    val rating: Int? = null,
    val comment: String? = null
) {
    init {
        require(perfumeId > 0) { "향수 ID는 0보다 커야 합니다." }
        
        if (feedbackType == FeedbackType.RATING) {
            require(rating != null && rating in 1..5) { 
                "평점 피드백의 경우 1-5 사이의 평점이 필요합니다." 
            }
        }
        
        comment?.let {
            require(it.length <= 1000) { "코멘트는 1000자를 초과할 수 없습니다." }
        }
    }
} 