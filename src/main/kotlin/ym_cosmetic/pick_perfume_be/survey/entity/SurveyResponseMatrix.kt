package ym_cosmetic.pick_perfume_be.survey.entity

import jakarta.persistence.*

@Entity
@Table(name = "survey_response_matrix")
class SurveyResponseMatrix(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "matrix_id")
    val matrixId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", nullable = false)
    val response: SurveyResponse,

    @Column(name = "option_key", nullable = false)
    val optionKey: String,

    @Column(name = "value", nullable = false)
    val value: Int
)