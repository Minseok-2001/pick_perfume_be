package ym_cosmetic.pick_perfume_be.review.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ym_cosmetic.pick_perfume_be.review.dto.ReviewReactionRequestDto
import ym_cosmetic.pick_perfume_be.review.dto.ReviewReactionResponseDto
import ym_cosmetic.pick_perfume_be.review.dto.ReviewReactionStatsDto
import ym_cosmetic.pick_perfume_be.review.service.ReviewReactionService
import ym_cosmetic.pick_perfume_be.security.UserPrincipal

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
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ReviewReactionResponseDto> {
        val reaction = reviewReactionService.reactToReview(userPrincipal.id, reviewId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(reaction)
    }

    @DeleteMapping
    @Operation(summary = "리뷰 리액션 삭제", description = "리뷰에 대한 리액션을 취소합니다.")
    fun deleteReaction(
        @PathVariable reviewId: Long,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<Unit> {
        reviewReactionService.deleteReaction(userPrincipal.id, reviewId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/stats")
    @Operation(summary = "리뷰 리액션 통계 조회", description = "리뷰의 좋아요/싫어요 통계를 조회합니다.")
    fun getReactionStats(
        @PathVariable reviewId: Long,
        @AuthenticationPrincipal userPrincipal: UserPrincipal?
    ): ResponseEntity<ReviewReactionStatsDto> {
        val stats = reviewReactionService.getReviewReactionStats(reviewId, userPrincipal?.id)
        return ResponseEntity.ok(stats)
    }
} 