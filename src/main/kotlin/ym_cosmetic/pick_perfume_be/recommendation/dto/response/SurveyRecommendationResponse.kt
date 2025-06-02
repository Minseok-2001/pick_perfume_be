package ym_cosmetic.pick_perfume_be.recommendation.dto.response

import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumeSummaryResponse
import ym_cosmetic.pick_perfume_be.recommendation.entity.SurveyRecommendation
import java.time.LocalDateTime

data class SurveyRecommendationResponse(
    val id: Long,
    val perfume: PerfumeSummaryResponse,
    val recommendationScore: Float,
    val recommendationRank: Int,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(
            surveyRecommendation: SurveyRecommendation,
            isLiked: Boolean = false
        ): SurveyRecommendationResponse {
            return SurveyRecommendationResponse(
                id = surveyRecommendation.id,
                perfume = PerfumeSummaryResponse.from(
                    perfume = surveyRecommendation.perfume,
                    isLiked = isLiked
                ),
                recommendationScore = surveyRecommendation.recommendationScore,
                recommendationRank = surveyRecommendation.recommendationRank,
                createdAt = surveyRecommendation.createdAt
            )
        }
    }
} 