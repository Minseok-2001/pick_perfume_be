package ym_cosmetic.pick_perfume_be.survey.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

/**
 * 설문 템플릿 엔티티
 * 설문 질문 정보를 저장합니다.
 */
@Entity
@Table(name = "survey_template")
class SurveyTemplate(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    val questionId: Long? = null,

    @Column(name = "question_key", nullable = false, unique = true)
    val questionKey: String,

    @Column(name = "question_text", nullable = false)
    val questionText: String,

    @Column(name = "question_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val questionType: QuestionType,

    @Column(name = "options", columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    val options: List<String>? = null,

    @Column(name = "max_selections")
    val maxSelections: Int? = null,

    @Column(name = "scale", columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    val scale: Scale? = null,

    @Column(name = "required", nullable = false)
    val required: Boolean = true,

    @Column(name = "sort_order", nullable = false)
    val sortOrder: Int
) {
    data class Scale(
        val min: Int,
        val max: Int
    )
}

