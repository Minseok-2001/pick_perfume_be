package ym_cosmetic.pick_perfume_be.review.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ym_cosmetic.pick_perfume_be.review.entity.ReviewReaction

@Repository
interface ReviewReactionRepository : JpaRepository<ReviewReaction, Long> {
    
    /**
     * 리뷰 ID로 좋아요/싫어요 수 조회
     */
    @Query("SELECT COUNT(rr) FROM ReviewReaction rr WHERE rr.review.id = :reviewId AND rr.isLike = true")
    fun countLikesByReviewId(@Param("reviewId") reviewId: Long): Long
    
    @Query("SELECT COUNT(rr) FROM ReviewReaction rr WHERE rr.review.id = :reviewId AND rr.isLike = false")
    fun countDislikesByReviewId(@Param("reviewId") reviewId: Long): Long
    
    /**
     * 특정 회원이 특정 리뷰에 남긴 리액션 조회
     */
    fun findByMemberIdAndReviewId(memberId: Long, reviewId: Long): ReviewReaction?
    
    /**
     * 특정 회원이 남긴 모든 리액션 조회
     */
    fun findByMemberId(memberId: Long): List<ReviewReaction>
    
    /**
     * 특정 리뷰에 대한 모든 리액션 조회
     */
    fun findByReviewId(reviewId: Long): List<ReviewReaction>
    
    /**
     * 특정 회원이 특정 리뷰에 리액션을 남겼는지 확인
     */
    fun existsByMemberIdAndReviewId(memberId: Long, reviewId: Long): Boolean
} 