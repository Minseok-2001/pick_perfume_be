package ym_cosmetic.pick_perfume_be.recommendation.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume
import ym_cosmetic.pick_perfume_be.survey.entity.Survey
import java.time.LocalDateTime

@Entity
@Table(name = "survey_recommendation")
class SurveyRecommendation private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    val survey: Survey,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    val member: Member?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfume_id", nullable = false)
    val perfume: Perfume,

    @Column(name = "recommendation_score", nullable = false)
    val recommendationScore: Float,

    @Column(name = "recommendation_rank", nullable = false)
    val recommendationRank: Int,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun create(
            survey: Survey,
            member: Member?,
            perfume: Perfume,
            recommendationScore: Float,
            recommendationRank: Int
        ): SurveyRecommendation {
            require(recommendationScore >= 0) { "추천 점수는 0 이상이어야 합니다." }
            require(recommendationRank > 0) { "추천 순위는 1 이상이어야 합니다." }
            
            return SurveyRecommendation(
                survey = survey,
                member = member,
                perfume = perfume,
                recommendationScore = recommendationScore,
                recommendationRank = recommendationRank
            )
        }
    }
} 