package ym_cosmetic.pick_perfume_be.survey.entity

import jakarta.persistence.*
import ym_cosmetic.pick_perfume_be.common.BaseTimeEntity
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume

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

    @Column(name = "slider_answer")
    val sliderAnswer: Int? = null,

    @OneToMany(mappedBy = "response", cascade = [CascadeType.ALL], orphanRemoval = true)
    val choiceAnswers: MutableList<SurveyResponseChoice> = mutableListOf(),

    @OneToMany(mappedBy = "response", cascade = [CascadeType.ALL], orphanRemoval = true)
    val matrixAnswers: MutableList<SurveyResponseMatrix> = mutableListOf()
) : BaseTimeEntity() {
    // 편의 메서드
    fun addChoiceAnswer(option: String): SurveyResponse {
        choiceAnswers.add(SurveyResponseChoice(response = this, optionText = option))
        return this
    }

    fun addMatrixAnswer(key: String, value: Int): SurveyResponse {
        matrixAnswers.add(SurveyResponseMatrix(response = this, optionKey = key, value = value))
        return this
    }

    fun addPerfumeRating(perfume: Perfume?, perfumeName: String, rating: Float, isCustom: Boolean): SurveyResponsePerfumeRating {
        val responseId = this.responseId ?: throw IllegalStateException("응답 ID가 없습니다.")
        return SurveyResponsePerfumeRating(
            responseId = responseId,
            perfume = perfume,
            perfumeName = perfumeName,
            rating = rating,
            isCustom = isCustom
        )
    }
}

