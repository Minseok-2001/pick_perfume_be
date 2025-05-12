package ym_cosmetic.pick_perfume_be.review.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import ym_cosmetic.pick_perfume_be.common.dto.response.ApiResponse
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.review.dto.ReviewReactionRequestDto
import ym_cosmetic.pick_perfume_be.review.dto.ReviewReactionResponseDto
import ym_cosmetic.pick_perfume_be.review.dto.ReviewReactionStatsDto
import ym_cosmetic.pick_perfume_be.review.service.ReviewReactionService
import ym_cosmetic.pick_perfume_be.security.CurrentMember

@RestController
@RequestMapping("/api/reviews/{reviewId}/reactions")
@Tag(name = "리뷰 리액션 API", description = "리뷰 좋아요/싫어요 관련 API")
class ReviewReactionController(
    private val reviewReactionService: ReviewReactionService
) {

    @PostMapping
    @Operation(summary = "리뷰 리액션 생성/수정", description = "리뷰에 좋아요/싫어요를 추가하거나 수정합니다.")
    fun reactToReview(
        @PathVariable reviewId: Long,
        @Valid @RequestBody request: ReviewReactionRequestDto,
        @CurrentMember member: Member
    ): ApiResponse<ReviewReactionResponseDto> {
        val reaction = reviewReactionService.reactToReview(member.id!!, reviewId, request)
        return ApiResponse.success(reaction)
    }

    @DeleteMapping
    @Operation(summary = "리뷰 리액션 삭제", description = "리뷰에 대한 리액션을 취소합니다.")
    fun deleteReaction(
        @PathVariable reviewId: Long,
        @CurrentMember member: Member
    ): ApiResponse<Unit> {
        reviewReactionService.deleteReaction(member.id!!, reviewId)
        return ApiResponse.success(Unit)
    }

    @GetMapping("/stats")
    @Operation(summary = "리뷰 리액션 통계 조회", description = "리뷰의 좋아요/싫어요 통계를 조회합니다.")
    fun getReactionStats(
        @PathVariable reviewId: Long,
        @CurrentMember member: Member
    ): ApiResponse<ReviewReactionStatsDto> {
        val stats = reviewReactionService.getReviewReactionStats(reviewId, member.id)
        return ApiResponse.success(stats)
    }
} 