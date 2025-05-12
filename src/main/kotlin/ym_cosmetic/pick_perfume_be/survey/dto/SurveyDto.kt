package ym_cosmetic.pick_perfume_be.survey.dto

import ym_cosmetic.pick_perfume_be.survey.entity.*
import java.time.LocalDateTime

/**
 * 설문 응답 DTO
 */
data class SurveyResponseDto(
    val surveyId: Long,
    val memberId: Long?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val imageUrl: String?,
    val status: SurveyStatus,
    val responses: List<SurveyAnswerDto>
) {
    companion object {
        fun fromEntity(survey: Survey, responses: List<SurveyAnswerDto>): SurveyResponseDto {
            return SurveyResponseDto(
                surveyId = survey.surveyId ?: 0L,
                memberId = survey.memberId,
                createdAt = survey.createdAt,
                updatedAt = survey.updatedAt,
                imageUrl = survey.imageUrl,
                status = survey.status,
                responses = responses
            )
        }
    }
}

/**
 * 간략한 설문 정보 DTO
 */
data class SurveySummary(
    val surveyId: Long,
    val memberId: Long?,
    val createdAt: LocalDateTime,
    val status: SurveyStatus,
    val responseCount: Int
) {
    companion object {
        fun fromEntity(survey: Survey): SurveySummary {
            return SurveySummary(
                surveyId = survey.surveyId ?: 0L,
                memberId = survey.memberId,
                createdAt = survey.createdAt,
                status = survey.status,
                responseCount = survey.responses.size
            )
        }
    }
}

/**
 * 설문 응답 항목 DTO
 */
data class SurveyAnswerDto(
    val responseId: Long?,
    val questionId: Long,
    val questionKey: String,
    val questionText: String,
    val questionType: QuestionType,
    val choiceAnswers: List<String>?,
    val sliderAnswer: Int?,
    val matrixAnswers: Map<String, Int>?
) {
    companion object {
        fun fromEntity(response: SurveyResponse): SurveyAnswerDto {
            return SurveyAnswerDto(
                responseId = response.responseId,
                questionId = response.question.questionId ?: 0L,
                questionKey = response.question.questionKey,
                questionText = response.question.questionText,
                questionType = response.question.questionType,
                choiceAnswers = response.choiceAnswers.map { it.optionText },
                sliderAnswer = response.sliderAnswer,
                matrixAnswers = response.matrixAnswers.associate { it.optionKey to it.value }
            )
        }
    }
}

/**
 * 설문 제출 요청 DTO
 */
data class SurveySubmitRequest(
    val memberId: Long?,
    val imageUrl: String?,
    val responses: List<ResponseSubmitDto>
)

/**
 * 응답 제출 DTO
 */
data class ResponseSubmitDto(
    val questionId: Long,
    val choiceAnswers: List<String>? = null,
    val sliderAnswer: Int? = null,
    val matrixAnswers: Map<String, Int>? = null
) {
    fun validate(question: SurveyTemplate): Boolean {
        // 질문 유형에 따른 응답 유효성 검사
        return when (question.questionType) {
            QuestionType.SINGLE_CHOICE -> choiceAnswers != null && choiceAnswers.size == 1
            QuestionType.MULTIPLE_CHOICE -> {
                choiceAnswers != null &&
                (question.maxSelections == null || choiceAnswers.size <= question.maxSelections!!)
            }
            QuestionType.SLIDER -> {
                sliderAnswer != null &&
                question.scale?.let { sliderAnswer in it.min..it.max } ?: false
            }
            QuestionType.MATRIX_SLIDER -> matrixAnswers != null && matrixAnswers.isNotEmpty()
            QuestionType.NUMERIC_INPUT -> {
                sliderAnswer != null &&
                question.scale?.let { sliderAnswer in it.min..it.max } ?: false
            }
            QuestionType.COLOR_PICKER -> {
                choiceAnswers != null && choiceAnswers.size == 1
            }
            QuestionType.PERFUME_RATING_SLIDER -> {
                sliderAnswer != null &&
                question.scale?.let { sliderAnswer in it.min..it.max } ?: false
            }
        }
    }
}

/**
 * 설문 분석 결과 DTO
 */
data class SurveyAnalysisResult(
    val surveyId: Long,
    val analyzedAt: LocalDateTime = LocalDateTime.now(),
    val recommendations: List<PerfumeRecommendation>
)

/**
 * 향수 추천 DTO
 */
data class PerfumeRecommendation(
    val perfumeId: Long,
    val name: String,
    val brand: String,
    val imageUrl: String?,
    val score: Double,
    val matchingFactors: List<String>
) 