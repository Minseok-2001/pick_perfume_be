package ym_cosmetic.pick_perfume_be.member.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ym_cosmetic.pick_perfume_be.member.entity.MemberActivity
import ym_cosmetic.pick_perfume_be.member.enums.ActivityType
import java.time.LocalDateTime

interface MemberActivityRepository : JpaRepository<MemberActivity, Long> {
    fun countByActivityTypeAndTimestampBetween(
        activityType: ActivityType,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Int

    @Query(
        """
    SELECT COUNT(DISTINCT ma1.memberId) FROM MemberActivity ma1
    WHERE ma1.activityType = :recommendationClickType
    AND ma1.timestamp BETWEEN :startDate AND :endDate
    AND EXISTS (
        SELECT 1 FROM MemberActivity ma2
        WHERE ma2.memberId = ma1.memberId
        AND ma2.perfumeId = ma1.perfumeId
        AND ma2.activityType IN (:conversionTypes)
        AND ma2.timestamp > ma1.timestamp
        AND ma2.timestamp <= :endDate
    )
    """
    )
    fun countRecommendationConversions(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime,
        @Param("recommendationClickType") recommendationClickType: ActivityType = ActivityType.RECOMMENDATION_CLICK,
        @Param("conversionTypes") conversionTypes: List<ActivityType> = listOf(
            ActivityType.REVIEW,
            ActivityType.VOTE
        )
    ): Int


}