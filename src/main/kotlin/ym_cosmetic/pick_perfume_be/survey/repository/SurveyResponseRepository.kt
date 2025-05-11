package ym_cosmetic.pick_perfume_be.survey.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ym_cosmetic.pick_perfume_be.survey.entity.SurveyResponse

@Repository
interface SurveyResponseRepository : JpaRepository<SurveyResponse, Long> {
    
    /**
     * 설문 ID로 응답 목록 조회
     */
    fun findBySurveySurveyId(surveyId: Long): List<SurveyResponse>
    
    /**
     * 질문 ID로 응답 목록 조회
     */
    fun findByQuestionQuestionId(questionId: Long): List<SurveyResponse>
    
    /**
     * 특정 설문의 특정 질문에 대한 응답 조회
     */
    fun findBySurveySurveyIdAndQuestionQuestionId(surveyId: Long, questionId: Long): SurveyResponse?
    
    /**
     * 특정 선택지를 선택한 응답 수 조회
     */
    @Query("SELECT COUNT(sr) FROM SurveyResponse sr WHERE sr.choiceAnswers LIKE %:option%")
    fun countByChoiceAnswersContaining(@Param("option") option: String): Long
} 