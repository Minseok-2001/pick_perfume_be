package ym_cosmetic.pick_perfume_be.survey.dto

import com.fasterxml.jackson.databind.ObjectMapper
import ym_cosmetic.pick_perfume_be.survey.entity.*

/**
 * 설문 템플릿 응답 DTO
 */
data class SurveyTemplateResponse(
    val questionId: Long,
    val questionKey: String,
    val questionText: String,
    val questionType: QuestionType,
    val options: Any?, // List<String> 또는 List<Map<String, Any>> 타입으로 변경
    val maxSelections: Int?,
    val scale: ScaleDto?,
    val required: Boolean,
    val sortOrder: Int
) {
    companion object {
        private val objectMapper = ObjectMapper()
        
        fun fromEntity(template: SurveyTemplate): SurveyTemplateResponse {
            // 옵션 처리 로직
            val options = when {
                template.options.isEmpty() -> null
                template.questionType == QuestionType.MATRIX_SLIDER -> {
                    // MATRIX_SLIDER인 경우 JSON 문자열을 객체로 파싱
                    template.options.map { option ->
                        try {
                            objectMapper.readValue(option.optionText, Map::class.java)
                        } catch (e: Exception) {
                            option.optionText
                        }
                    }
                }
                else -> {
                    // 일반적인 경우 텍스트 옵션 목록 반환
                    template.options.sortedBy { it.sortOrder }.map { it.optionText }
                }
            }
            
            return SurveyTemplateResponse(
                questionId = template.questionId ?: 0L,
                questionKey = template.questionKey,
                questionText = template.questionText,
                questionType = template.questionType,
                options = options,
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
    val max: Int,
    val step: Double? = null,
    val labels: List<String>? = null
) {
    companion object {
        private val objectMapper = ObjectMapper()
        
        fun fromEntity(scale: SurveyTemplateScale): ScaleDto {
            val parsedLabels = try {
                scale.labels?.let { objectMapper.readValue(it, Array<String>::class.java)?.toList() }
            } catch (e: Exception) {
                null
            }
            
            return ScaleDto(
                min = scale.min,
                max = scale.max,
                step = scale.step,
                labels = parsedLabels
            )
        }
    }
}

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
        val template = SurveyTemplate(
            questionKey = questionKey,
            questionText = questionText,
            questionType = questionType,
            maxSelections = maxSelections,
            required = required,
            sortOrder = sortOrder
        )
        
        // 옵션 추가
        options?.forEach { template.addOption(it) }
        
        // 스케일 추가
        scale?.let { template.setScale(it.min, it.max) }
        
        return template
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
) 