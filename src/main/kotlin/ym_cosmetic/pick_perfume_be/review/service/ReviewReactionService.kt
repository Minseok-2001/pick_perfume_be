package ym_cosmetic.pick_perfume_be.review.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ym_cosmetic.pick_perfume_be.common.exception.EntityNotFoundException
import ym_cosmetic.pick_perfume_be.member.repository.MemberRepository
import ym_cosmetic.pick_perfume_be.review.dto.ReviewReactionRequestDto
import ym_cosmetic.pick_perfume_be.review.dto.ReviewReactionResponseDto
import ym_cosmetic.pick_perfume_be.review.dto.ReviewReactionStatsDto
import ym_cosmetic.pick_perfume_be.review.entity.ReviewReaction
import ym_cosmetic.pick_perfume_be.review.repository.ReviewReactionRepository
import ym_cosmetic.pick_perfume_be.review.repository.ReviewRepository

@Service
class ReviewReactionService(
    private val reviewReactionRepository: ReviewReactionRepository,
    private val reviewRepository: ReviewRepository,
    private val memberRepository: MemberRepository
) {

    /**
     * 리뷰에 리액션 추가 또는 수정
     */
    @Transactional
    fun reactToReview(memberId: Long, reviewId: Long, request: ReviewReactionRequestDto): ReviewReactionResponseDto {
        val member = memberRepository.findById(memberId)
            .orElseThrow { EntityNotFoundException("회원", memberId) }
        
        val review = reviewRepository.findById(reviewId)
            .orElseThrow { EntityNotFoundException("리뷰", reviewId) }
        
        // 기존 리액션이 있는지 확인
        val existingReaction = reviewReactionRepository.findByMemberIdAndReviewId(memberId, reviewId)
        
        val reaction = if (existingReaction != null) {
            // 기존 리액션이 있으면 업데이트
            existingReaction.isLike = request.isLike
            existingReaction
        } else {
            // 새로운 리액션 생성
            ReviewReaction(
                member = member,
                review = review,
                isLike = request.isLike
            )
        }
        
        val savedReaction = reviewReactionRepository.save(reaction)
        return ReviewReactionResponseDto.from(savedReaction)
    }

    /**
     * 리뷰 리액션 삭제
     */
    @Transactional
    fun deleteReaction(memberId: Long, reviewId: Long) {
        val reaction = reviewReactionRepository.findByMemberIdAndReviewId(memberId, reviewId)
            ?: throw EntityNotFoundException("해당 리뷰에 대한 리액션을 찾을 수 없습니다.")
        
        reviewReactionRepository.delete(reaction)
    }

    /**
     * 리뷰의 좋아요 수 조회
     */
    @Transactional(readOnly = true)
    fun countLikesByReviewId(reviewId: Long): Long {
        return reviewReactionRepository.countLikesByReviewId(reviewId)
    }

    /**
     * 리뷰의 싫어요 수 조회
     */
    @Transactional(readOnly = true)
    fun countDislikesByReviewId(reviewId: Long): Long {
        return reviewReactionRepository.countDislikesByReviewId(reviewId)
    }

    /**
     * 특정 리뷰에 대한 사용자의 리액션 조회
     */
    @Transactional(readOnly = true)
    fun getUserReactionForReview(memberId: Long, reviewId: Long): Boolean? {
        val reaction = reviewReactionRepository.findByMemberIdAndReviewId(memberId, reviewId)
        return reaction?.isLike
    }

    /**
     * 리뷰 리액션 통계 조회
     */
    @Transactional(readOnly = true)
    fun getReviewReactionStats(reviewId: Long, currentUserId: Long?): ReviewReactionStatsDto {
        val likeCount = countLikesByReviewId(reviewId)
        val dislikeCount = countDislikesByReviewId(reviewId)
        val currentUserReaction = if (currentUserId != null) {
            getUserReactionForReview(currentUserId, reviewId)
        } else {
            null
        }
        
        return ReviewReactionStatsDto(
            reviewId = reviewId,
            likeCount = likeCount,
            dislikeCount = dislikeCount,
            currentUserReaction = currentUserReaction
        )
    }
} 