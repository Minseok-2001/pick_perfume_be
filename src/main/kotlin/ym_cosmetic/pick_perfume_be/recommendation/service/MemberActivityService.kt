package ym_cosmetic.pick_perfume_be.recommendation.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ym_cosmetic.pick_perfume_be.common.event.PerfumeViewedEvent
import ym_cosmetic.pick_perfume_be.common.event.RecommendationClickedEvent
import ym_cosmetic.pick_perfume_be.common.event.ReviewCreatedEvent
import ym_cosmetic.pick_perfume_be.common.event.VoteCreatedEvent
import ym_cosmetic.pick_perfume_be.member.entity.MemberActivity
import ym_cosmetic.pick_perfume_be.member.enums.ActivityType
import ym_cosmetic.pick_perfume_be.member.repository.MemberActivityRepository
import java.time.LocalDateTime

@Service
class MemberActivityService(
    private val memberActivityRepository: MemberActivityRepository,
    private val memberPreferenceService: MemberPreferenceService
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    companion object {
        private val logger = LoggerFactory.getLogger(MemberActivityService::class.java)
    }

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
    @EventListener
    @Transactional
    fun handlePerfumeViewedEvent(event: PerfumeViewedEvent) {
            try {
                recordActivity(
                    memberId = event.memberId,
                    activityType = ActivityType.VIEW,
                    perfumeId = event.perfumeId
                )
            } catch (e: Exception) {
                logger.error("Failed to record perfume viewed activity: ${e.message}", e)
                // 실패한 이벤트를 재처리 큐에 넣거나 알림 발송
            }
    }

    /**
     * 추천 클릭 이벤트 처리
     */
    @EventListener
    @Transactional
    fun handleRecommendationClickedEvent(event: RecommendationClickedEvent) {
        logger.info("추천 클릭 이벤트 수신: memberId=${event.memberId}, perfumeId=${event.perfumeId}")
        recordActivity(
                memberId = event.memberId,
                activityType = ActivityType.RECOMMENDATION_CLICK,
                perfumeId = event.perfumeId,
                recommendationType = event.recommendationType
            )

    }

    /**
     * 리뷰 생성 이벤트 처리
     */
    @EventListener
    @Transactional
    fun handleReviewCreatedEvent(event: ReviewCreatedEvent) {
            recordActivity(
                memberId = event.memberId,
                activityType = ActivityType.REVIEW,
                perfumeId = event.perfumeId,
                rating = event.rating
            )
    }

    /**
     * 투표 생성 이벤트 처리
     */
    @EventListener
    @Transactional
    fun handleVoteCreatedEvent(event: VoteCreatedEvent) {
            recordActivity(
                memberId = event.memberId,
                activityType = ActivityType.VOTE,
                perfumeId = event.perfumeId,
                voteCategory = event.category,
                voteValue = event.value
            )
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