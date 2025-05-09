package ym_cosmetic.pick_perfume_be.recommendation.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionalEventListener
import ym_cosmetic.pick_perfume_be.common.event.ReviewCreatedEvent
import ym_cosmetic.pick_perfume_be.common.event.VoteCreatedEvent
import ym_cosmetic.pick_perfume_be.member.entity.MemberActivity
import ym_cosmetic.pick_perfume_be.member.enums.ActivityType
import ym_cosmetic.pick_perfume_be.member.repository.MemberActivityRepository
import ym_cosmetic.pick_perfume_be.recommendation.event.PerfumeViewedEvent
import ym_cosmetic.pick_perfume_be.recommendation.event.RecommendationClickedEvent
import java.time.LocalDateTime

@Service
class MemberActivityService(
    private val memberActivityRepository: MemberActivityRepository,
    private val memberPreferenceService: MemberPreferenceService
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    /**
     * 활동 기록
     */
    @Transactional
    fun recordActivity(
        memberId: Long,
        activityType: ActivityType,
        perfumeId: Long,
        rating: Int? = null,
        voteCategory: String? = null,
        voteValue: String? = null,
        recommendationType: String? = null,
        timestamp: LocalDateTime = LocalDateTime.now()
    ) {
        val activity = MemberActivity(
            memberId = memberId,
            activityType = activityType,
            perfumeId = perfumeId,
            rating = rating,
            voteCategory = voteCategory,
            voteValue = voteValue,
            recommendationType = recommendationType,
            timestamp = timestamp
        )

        memberActivityRepository.save(activity)
    }

    /**
     * 향수 조회 이벤트 처리
     */
    @TransactionalEventListener
    fun handlePerfumeViewedEvent(event: PerfumeViewedEvent) {
        coroutineScope.launch {
            recordActivity(
                memberId = event.memberId,
                activityType = ActivityType.VIEW,
                perfumeId = event.perfumeId
            )
        }
    }

    /**
     * 추천 클릭 이벤트 처리
     */
    @TransactionalEventListener
    fun handleRecommendationClickedEvent(event: RecommendationClickedEvent) {
        coroutineScope.launch {
            recordActivity(
                memberId = event.memberId,
                activityType = ActivityType.RECOMMENDATION_CLICK,
                perfumeId = event.perfumeId,
                recommendationType = event.recommendationType
            )
        }
    }

    /**
     * 리뷰 생성 이벤트 처리
     */
    @TransactionalEventListener
    fun handleReviewCreatedEvent(event: ReviewCreatedEvent) {
        coroutineScope.launch {
            recordActivity(
                memberId = event.memberId,
                activityType = ActivityType.REVIEW,
                perfumeId = event.perfumeId,
                rating = event.rating
            )
        }
    }

    /**
     * 투표 생성 이벤트 처리
     */
    @TransactionalEventListener
    fun handleVoteCreatedEvent(event: VoteCreatedEvent) {
        coroutineScope.launch {
            recordActivity(
                memberId = event.memberId,
                activityType = ActivityType.VOTE,
                perfumeId = event.perfumeId,
                voteCategory = event.category,
                voteValue = event.value
            )
        }
    }

    /**
     * 추천 클릭률(CTR) 계산
     */
    fun calculateClickThroughRate(days: Int = 30): Double {
        val endDate = LocalDateTime.now()
        val startDate = endDate.minusDays(days.toLong())

        val impressions = memberActivityRepository.countByActivityTypeAndTimestampBetween(
            activityType = ActivityType.RECOMMENDATION_IMPRESSION,
            startDate = startDate,
            endDate = endDate
        )

        val clicks = memberActivityRepository.countByActivityTypeAndTimestampBetween(
            activityType = ActivityType.RECOMMENDATION_CLICK,
            startDate = startDate,
            endDate = endDate
        )

        if (impressions == 0) return 0.0

        return clicks.toDouble() / impressions.toDouble()
    }

    /**
     * 추천 전환율 계산
     */
    fun calculateConversionRate(days: Int = 30): Double {
        val endDate = LocalDateTime.now()
        val startDate = endDate.minusDays(days.toLong())

        val clicks = memberActivityRepository.countByActivityTypeAndTimestampBetween(
            activityType = ActivityType.RECOMMENDATION_CLICK,
            startDate = startDate,
            endDate = endDate
        )

        val conversions = memberActivityRepository.countRecommendationConversions(
            startDate = startDate,
            endDate = endDate
        )

        if (clicks == 0) return 0.0

        return conversions.toDouble() / clicks.toDouble()
    }

    /**
     * 추천 품질 분석
     */
    fun analyzeRecommendationQuality() {
        coroutineScope.launch {
            // 클릭률 분석
            val ctr = calculateClickThroughRate()

            // 전환율 분석
            val conversionRate = calculateConversionRate()

            // 분석 결과 저장 또는 로깅
            println("Recommendation CTR: $ctr, Conversion Rate: $conversionRate")
        }
    }
}