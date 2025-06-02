package ym_cosmetic.pick_perfume_be.recommendation.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ym_cosmetic.pick_perfume_be.recommendation.entity.SurveyRecommendation

interface SurveyRecommendationRepository : JpaRepository<SurveyRecommendation, Long> {
    
    @Query("SELECT sr FROM SurveyRecommendation sr WHERE sr.survey.surveyId = :surveyId ORDER BY sr.recommendationRank")
    fun findBySurveyIdOrderByRank(@Param("surveyId") surveyId: Long): List<SurveyRecommendation>
    
    @Query("SELECT sr FROM SurveyRecommendation sr WHERE sr.survey.surveyId = :surveyId AND sr.member.id = :memberId ORDER BY sr.recommendationRank")
    fun findBySurveyIdAndMemberIdOrderByRank(
        @Param("surveyId") surveyId: Long, 
        @Param("memberId") memberId: Long
    ): List<SurveyRecommendation>
    
    @Query("SELECT sr FROM SurveyRecommendation sr WHERE sr.survey.surveyId = :surveyId AND sr.perfume.id = :perfumeId")
    fun findBySurveyIdAndPerfumeId(
        @Param("surveyId") surveyId: Long,
        @Param("perfumeId") perfumeId: Long
    ): SurveyRecommendation?
} 