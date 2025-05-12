package ym_cosmetic.pick_perfume_be.survey.entity

import jakarta.persistence.*

@Entity
@Table(name = "survey_template_scale")
class SurveyTemplateScale(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scale_id")
    val scaleId: Long? = null,
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false, unique = true)
    val template: SurveyTemplate,
    
    @Column(name = "min_value", nullable = false)
    val min: Int,
    
    @Column(name = "max_value", nullable = false)
    val max: Int,
    
    @Column(name = "step_value")
    val step: Double? = null,
    
    @Column(name = "labels", columnDefinition = "TEXT")
    val labels: String? = null
)

