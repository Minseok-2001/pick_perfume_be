package ym_cosmetic.pick_perfume_be.survey.entity

import jakarta.persistence.*
import ym_cosmetic.pick_perfume_be.common.BaseTimeEntity

/**
 * 설문 엔티티
 * 사용자가 제출한 설문 정보를 저장합니다.
 */
@Entity
@Table(name = "survey")
class Survey(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "survey_id")
    val surveyId: Long? = null,

    @Column(name = "member_id")
    val memberId: Long? = null,

    @Column(name = "session_id")
    var sessionId: String? = null,

    @Column(name = "image_url")
    val imageUrl: String? = null,

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    val status: SurveyStatus = SurveyStatus.SUBMITTED,

    @OneToMany(mappedBy = "survey", cascade = [CascadeType.ALL], orphanRemoval = true)
    val responses: MutableList<SurveyResponse> = mutableListOf()
) : BaseTimeEntity()

