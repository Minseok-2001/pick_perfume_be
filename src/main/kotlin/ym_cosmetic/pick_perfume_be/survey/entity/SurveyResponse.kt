package ym_cosmetic.pick_perfume_be.survey.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import ym_cosmetic.pick_perfume_be.common.BaseTimeEntity

/**
 * 설문 응답 엔티티
 * 각 질문에 대한 사용자의 응답을 저장합니다.
 */
@Entity
@Table(name = "survey_response")
class SurveyResponse(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "response_id")
    val responseId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    val survey: Survey,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    val question: SurveyTemplate,

    @Column(name = "choice_answers", columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    val choiceAnswers: List<String>? = null,

    @Column(name = "slider_answer")
    val sliderAnswer: Int? = null,

    @Column(name = "matrix_answers", columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    val matrixAnswers: Map<String, Int>? = null
) : BaseTimeEntity() {
    companion object {
        fun fromEntity(response: SurveyResponse): SurveyResponse {
            return SurveyResponse(
                responseId = response.responseId,
                survey = response.survey,
                question = response.question,
                choiceAnswers = response.choiceAnswers,
                sliderAnswer = response.sliderAnswer,
                matrixAnswers = response.matrixAnswers
            )
        }
    }
}