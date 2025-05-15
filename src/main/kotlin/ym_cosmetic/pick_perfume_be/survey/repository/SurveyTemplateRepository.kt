package ym_cosmetic.pick_perfume_be.survey.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ym_cosmetic.pick_perfume_be.survey.entity.QuestionType
import ym_cosmetic.pick_perfume_be.survey.entity.SurveyTemplate

@Repository
interface SurveyTemplateRepository : JpaRepository<SurveyTemplate, Long> {

    /**
     * 질문 키로 템플릿 조회
     */
    fun findByQuestionKey(questionKey: String): SurveyTemplate?

    /**
     * 정렬 순서대로 템플릿 목록 조회
     */
    fun findAllByOrderBySortOrderAsc(): List<SurveyTemplate>

    /**
     * 질문 유형별 템플릿 목록 조회
     */
    fun findByQuestionType(questionType: QuestionType): List<SurveyTemplate>

    /**
     * 필수 여부별 템플릿 목록 조회
     */
    fun findByRequired(required: Boolean): List<SurveyTemplate>
} 