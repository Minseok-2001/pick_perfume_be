package ym_cosmetic.pick_perfume_be.survey.entity

import jakarta.persistence.*

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

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    val questionText: String,

    @Column(name = "question_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val questionType: QuestionType,

    @Column(name = "max_selections")
    val maxSelections: Int? = null,

    @Column(name = "required", nullable = false)
    val required: Boolean = true,

    @Column(name = "sort_order", nullable = false)
    val sortOrder: Int,
    
    @OneToMany(mappedBy = "template", cascade = [CascadeType.ALL], orphanRemoval = true)
    val options: MutableList<SurveyTemplateOption> = mutableListOf(),
    
    @OneToOne(mappedBy = "template", cascade = [CascadeType.ALL], orphanRemoval = true)
    var scale: SurveyTemplateScale? = null
) {
    // 편의 메서드
    fun addOption(option: String): SurveyTemplate {
        options.add(SurveyTemplateOption(template = this, optionText = option, sortOrder = options.size))
        return this
    }
    
    fun setScale(min: Int, max: Int): SurveyTemplate {
        scale = SurveyTemplateScale(template = this, min = min, max = max)
        return this
    }
}


