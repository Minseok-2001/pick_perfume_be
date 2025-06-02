package ym_cosmetic.pick_perfume_be.recommendation.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.recommendation.enums.FeedbackType
import java.time.LocalDateTime

@Entity
@Table(name = "survey_recommendation_feedback")
class SurveyRecommendationFeedback private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_recommendation_id", nullable = false)
    val surveyRecommendation: SurveyRecommendation,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    val member: Member?,

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_type", nullable = false)
    val feedbackType: FeedbackType,

    @Column(name = "rating", nullable = true)
    val rating: Int?,

    @Column(name = "comment", length = 1000)
    val comment: String?,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun create(
            surveyRecommendation: SurveyRecommendation,
            member: Member?,
            feedbackType: FeedbackType,
            rating: Int? = null,
            comment: String? = null
        ): SurveyRecommendationFeedback {
            // 평점 피드백인 경우 rating 필수
            if (feedbackType == FeedbackType.RATING) {
                require(rating != null && rating in 1..5) { 
                    "평점 피드백의 경우 1-5 사이의 평점이 필요합니다." 
                }
            }
            
            return SurveyRecommendationFeedback(
                surveyRecommendation = surveyRecommendation,
                member = member,
                feedbackType = feedbackType,
                rating = rating,
                comment = comment
            )
        }
    }
} 