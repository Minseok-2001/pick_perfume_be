package ym_cosmetic.pick_perfume_be.survey.entity

import jakarta.persistence.*

@Entity
@Table(name = "survey_template_option")
class SurveyTemplateOption(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    val optionId: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    val template: SurveyTemplate,
    
    @Column(name = "option_text", nullable = false, columnDefinition = "TEXT")
    val optionText: String,
    
    @Column(name = "sort_order", nullable = false)
    val sortOrder: Int
)
