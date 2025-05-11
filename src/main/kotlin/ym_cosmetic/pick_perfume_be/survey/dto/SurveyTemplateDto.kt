package ym_cosmetic.pick_perfume_be.survey.dto

import ym_cosmetic.pick_perfume_be.survey.entity.QuestionType
import ym_cosmetic.pick_perfume_be.survey.entity.SurveyTemplate

/**
 * 설문 템플릿 응답 DTO
 */
data class SurveyTemplateResponse(
    val questionId: Long,
    val questionKey: String,
    val questionText: String,
    val questionType: QuestionType,
    val options: List<String>?,
    val maxSelections: Int?,
    val scale: ScaleDto?,
    val required: Boolean,
    val sortOrder: Int
) {
    companion object {
        fun fromEntity(template: SurveyTemplate): SurveyTemplateResponse {
            return SurveyTemplateResponse(
                questionId = template.questionId ?: 0L,
                questionKey = template.questionKey,
                questionText = template.questionText,
                questionType = template.questionType,
                options = template.options,
                maxSelections = template.maxSelections,
                scale = template.scale?.let { ScaleDto(it.min, it.max) },
                required = template.required,
                sortOrder = template.sortOrder
            )
        }
    }
}

/**
 * 슬라이더 범위 DTO
 */
data class ScaleDto(
    val min: Int,
    val max: Int
)

/**
 * 설문 템플릿 생성 요청 DTO
 */
data class SurveyTemplateCreateRequest(
    val questionKey: String,
    val questionText: String,
    val questionType: QuestionType,
    val options: List<String>? = null,
    val maxSelections: Int? = null,
    val scale: ScaleDto? = null,
    val required: Boolean = true,
    val sortOrder: Int
) {
    fun toEntity(): SurveyTemplate {
        return SurveyTemplate(
            questionKey = questionKey,
            questionText = questionText,
            questionType = questionType,
            options = options,
            maxSelections = maxSelections,
            scale = scale?.let { SurveyTemplate.Scale(it.min, it.max) },
            required = required,
            sortOrder = sortOrder
        )
    }
}

/**
 * 설문 템플릿 수정 요청 DTO
 */
data class SurveyTemplateUpdateRequest(
    val questionText: String,
    val questionType: QuestionType,
    val options: List<String>? = null,
    val maxSelections: Int? = null,
    val scale: ScaleDto? = null,
    val required: Boolean = true,
    val sortOrder: Int
) {
    fun toEntity(questionKey: String): SurveyTemplate {
        return SurveyTemplate(
            questionKey = questionKey,
            questionText = questionText,
            questionType = questionType,
            options = options,
            maxSelections = maxSelections,
            scale = scale?.let { SurveyTemplate.Scale(it.min, it.max) },
            required = required,
            sortOrder = sortOrder
        )
    }
} 