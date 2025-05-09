package ym_cosmetic.pick_perfume_be.review.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ym_cosmetic.pick_perfume_be.review.entity.Review

interface ReviewRepository : JpaRepository<Review, Long> {
    @Query("SELECT r FROM Review r WHERE r.member.id = :memberId AND r.rating.value >= :minRating")
    fun findByMemberIdAndRatingValueGreaterThanEqual(
        @Param("memberId") memberId: Long,
        @Param("minRating") minRating: Int
    ): List<Review>

    fun findByMemberId(memberId: Long): List<Review>
}