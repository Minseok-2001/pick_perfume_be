package ym_cosmetic.pick_perfume_be.survey.entity

import jakarta.persistence.*
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume

@Entity
@Table(name = "survey_response_perfume_rating")
class SurveyResponsePerfumeRating(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rating_id")
    val ratingId: Long? = null,

    @Column(name = "response_id", nullable = false)
    val responseId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfume_id")
    var perfume: Perfume? = null,

    @Column(name = "perfume_name", nullable = false)
    val perfumeName: String,

    @Column(name = "rating", nullable = false)
    val rating: Float,

    @Column(name = "is_custom", nullable = false)
    val isCustom: Boolean = false
)