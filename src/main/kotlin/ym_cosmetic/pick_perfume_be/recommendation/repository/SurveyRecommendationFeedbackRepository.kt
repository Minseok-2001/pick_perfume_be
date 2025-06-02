package ym_cosmetic.pick_perfume_be.recommendation.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ym_cosmetic.pick_perfume_be.recommendation.entity.SurveyRecommendationFeedback
import ym_cosmetic.pick_perfume_be.recommendation.enums.FeedbackType

interface SurveyRecommendationFeedbackRepository : JpaRepository<SurveyRecommendationFeedback, Long> {
    
    @Query("SELECT srf FROM SurveyRecommendationFeedback srf WHERE srf.surveyRecommendation.id = :recommendationId")
    fun findByRecommendationId(@Param("recommendationId") recommendationId: Long): List<SurveyRecommendationFeedback>
    
    @Query("SELECT srf FROM SurveyRecommendationFeedback srf WHERE srf.surveyRecommendation.id = :recommendationId AND srf.member.id = :memberId")
    fun findByRecommendationIdAndMemberId(
        @Param("recommendationId") recommendationId: Long,
        @Param("memberId") memberId: Long
    ): List<SurveyRecommendationFeedback>
    
    @Query("SELECT srf FROM SurveyRecommendationFeedback srf WHERE srf.surveyRecommendation.id = :recommendationId AND srf.member.id = :memberId AND srf.feedbackType = :feedbackType")
    fun findByRecommendationIdAndMemberIdAndFeedbackType(
        @Param("recommendationId") recommendationId: Long,
        @Param("memberId") memberId: Long,
        @Param("feedbackType") feedbackType: FeedbackType
    ): SurveyRecommendationFeedback?
    
    fun existsByMemberIdAndSurveyRecommendationIdAndFeedbackType(
        memberId: Long,
        surveyRecommendationId: Long,
        feedbackType: FeedbackType
    ): Boolean
} 