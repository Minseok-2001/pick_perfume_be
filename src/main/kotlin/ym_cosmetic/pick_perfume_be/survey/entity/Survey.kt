package ym_cosmetic.pick_perfume_be.survey.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

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

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "image_url")
    val imageUrl: String? = null,

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    val status: SurveyStatus = SurveyStatus.SUBMITTED,

    @OneToMany(mappedBy = "survey", cascade = [CascadeType.ALL], orphanRemoval = true)
    val responses: MutableList<SurveyResponse> = mutableListOf()
)

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

    @Column(name = "choice_answers", columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    val choiceAnswers: List<String>? = null,

    @Column(name = "slider_answer")
    val sliderAnswer: Int? = null,

    @Column(name = "matrix_answers", columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    val matrixAnswers: Map<String, Int>? = null
)

/**
 * 설문 상태
 */
enum class SurveyStatus {
    DRAFT,      // 임시 저장
    SUBMITTED,  // 제출됨
    PROCESSED   // 처리됨 (모델 분석 완료)
} 