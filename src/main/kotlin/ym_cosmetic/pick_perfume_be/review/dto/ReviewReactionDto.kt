package ym_cosmetic.pick_perfume_be.review.dto

import jakarta.validation.constraints.NotNull
import ym_cosmetic.pick_perfume_be.review.entity.ReviewReaction

/**
 * 리뷰 리액션 요청 DTO
 */
data class ReviewReactionRequestDto(
    @field:NotNull(message = "리액션 타입은 필수입니다.")
    val isLike: Boolean
)

/**
 * 리뷰 리액션 응답 DTO
 */
data class ReviewReactionResponseDto(
    val id: Long,
    val reviewId: Long,
    val memberId: Long,
    val isLike: Boolean
) {
    companion object {
        fun from(reaction: ReviewReaction): ReviewReactionResponseDto {
            return ReviewReactionResponseDto(
                id = reaction.id!!,
                reviewId = reaction.review.id!!,
                memberId = reaction.member.id!!,
                isLike = reaction.isLike
            )
        }
    }
}

/**
 * 리뷰 리액션 통계 DTO
 */
data class ReviewReactionStatsDto(
    val reviewId: Long,
    val likeCount: Long,
    val dislikeCount: Long,
    val currentUserReaction: Boolean?
) 