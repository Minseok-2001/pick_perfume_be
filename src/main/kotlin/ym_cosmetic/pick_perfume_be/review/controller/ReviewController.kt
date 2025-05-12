package ym_cosmetic.pick_perfume_be.review.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ym_cosmetic.pick_perfume_be.review.dto.*
import ym_cosmetic.pick_perfume_be.review.service.ReviewService
import ym_cosmetic.pick_perfume_be.security.UserPrincipal

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "리뷰 API", description = "향수 리뷰 조회, 생성, 수정, 삭제 API")
class ReviewController(
    private val reviewService: ReviewService
) {

    @GetMapping("/{id}")
    @Operation(summary = "리뷰 상세 조회", description = "특정 리뷰의 상세 정보를 조회합니다.")
    fun getReviewById(
        @PathVariable("id") reviewId: Long,
        @AuthenticationPrincipal userPrincipal: UserPrincipal?
    ): ResponseEntity<ReviewResponseDto> {
        val review = reviewService.getReviewById(reviewId, userPrincipal?.id)
        return ResponseEntity.ok(review)
    }

    @PostMapping
    @Operation(summary = "리뷰 작성", description = "새로운 향수 리뷰를 작성합니다.")
    fun createReview(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: ReviewCreateRequestDto
    ): ResponseEntity<ReviewResponseDto> {
        val createdReview = reviewService.createReview(userPrincipal.id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReview)
    }

    @PutMapping("/{id}")
    @Operation(summary = "리뷰 수정", description = "기존 리뷰를 수정합니다.")
    fun updateReview(
        @PathVariable("id") reviewId: Long,
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: ReviewUpdateRequestDto
    ): ResponseEntity<ReviewResponseDto> {
        val updatedReview = reviewService.updateReview(reviewId, userPrincipal.id, request)
        return ResponseEntity.ok(updatedReview)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "리뷰 삭제", description = "특정 리뷰를 삭제합니다.")
    fun deleteReview(
        @PathVariable("id") reviewId: Long,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<Unit> {
        reviewService.deleteReview(reviewId, userPrincipal.id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    @Operation(summary = "리뷰 검색", description = "다양한 조건으로 리뷰를 검색합니다.")
    fun searchReviews(
        searchDto: ReviewSearchDto,
        @AuthenticationPrincipal userPrincipal: UserPrincipal?
    ): ResponseEntity<Page<ReviewSummaryDto>> {
        val reviews = reviewService.searchReviews(searchDto, userPrincipal?.id)
        return ResponseEntity.ok(reviews)
    }

    @GetMapping("/perfume/{id}")
    @Operation(summary = "향수별 리뷰 목록 조회", description = "특정 향수에 대한 리뷰 목록을 조회합니다.")
    fun getReviewsByPerfumeId(
        @PathVariable("id") perfumeId: Long,
        @PageableDefault(size = 10, sort = ["createdAt"]) pageable: Pageable,
        @AuthenticationPrincipal userPrincipal: UserPrincipal?
    ): ResponseEntity<Page<ReviewSummaryDto>> {
        val reviews = reviewService.getReviewsByPerfumeId(perfumeId, pageable, userPrincipal?.id)
        return ResponseEntity.ok(reviews)
    }

    @GetMapping("/member/{id}")
    @Operation(summary = "회원별 리뷰 목록 조회", description = "특정 회원이 작성한 리뷰 목록을 조회합니다.")
    fun getReviewsByMemberId(
        @PathVariable("id") memberId: Long,
        @PageableDefault(size = 10, sort = ["createdAt"]) pageable: Pageable
    ): ResponseEntity<Page<ReviewSummaryDto>> {
        val reviews = reviewService.getReviewsByMemberId(memberId, pageable)
        return ResponseEntity.ok(reviews)
    }

    @GetMapping("/me")
    @Operation(summary = "내 리뷰 목록 조회", description = "현재 로그인한 회원이 작성한 리뷰 목록을 조회합니다.")
    fun getMyReviews(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PageableDefault(size = 10, sort = ["createdAt"]) pageable: Pageable
    ): ResponseEntity<Page<ReviewSummaryDto>> {
        val reviews = reviewService.getReviewsByMemberId(userPrincipal.id, pageable)
        return ResponseEntity.ok(reviews)
    }
} 