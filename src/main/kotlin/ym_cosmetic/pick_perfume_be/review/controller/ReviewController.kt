package ym_cosmetic.pick_perfume_be.review.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*
import ym_cosmetic.pick_perfume_be.common.dto.response.ApiResponse
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.review.dto.*
import ym_cosmetic.pick_perfume_be.review.service.ReviewService
import ym_cosmetic.pick_perfume_be.security.CurrentMember

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
        @CurrentMember member: Member
    ): ApiResponse<ReviewResponseDto> {
        val review = reviewService.getReviewById(reviewId, member.id)
        return ApiResponse.success(review)
    }

    @PostMapping
    @Operation(summary = "리뷰 작성", description = "새로운 향수 리뷰를 작성합니다.")
    fun createReview(
        @CurrentMember member: Member,
        @Valid @RequestBody request: ReviewCreateRequestDto
    ): ApiResponse<ReviewResponseDto> {
        val createdReview = reviewService.createReview(member.id!!, request)
        return ApiResponse.success(createdReview)
    }

    @PutMapping("/{id}")
    @Operation(summary = "리뷰 수정", description = "기존 리뷰를 수정합니다.")
    fun updateReview(
        @PathVariable("id") reviewId: Long,
        @CurrentMember member: Member,
        @Valid @RequestBody request: ReviewUpdateRequestDto
    ): ApiResponse<ReviewResponseDto> {
        val updatedReview = reviewService.updateReview(reviewId, member.id!!, request)
        return ApiResponse.success(updatedReview)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "리뷰 삭제", description = "특정 리뷰를 삭제합니다.")
    fun deleteReview(
        @PathVariable("id") reviewId: Long,
        @CurrentMember member: Member
    ): ApiResponse<Unit> {
        reviewService.deleteReview(reviewId, member.id!!)
        return ApiResponse.success(Unit)
    }

    @GetMapping
    @Operation(summary = "리뷰 검색", description = "다양한 조건으로 리뷰를 검색합니다.")
    fun searchReviews(
        searchDto: ReviewSearchDto,
        @CurrentMember member: Member
    ): ApiResponse<Page<ReviewSummaryDto>> {
        val reviews = reviewService.searchReviews(searchDto, member.id)
        return ApiResponse.success(reviews)
    }

    @GetMapping("/perfume/{id}")
    @Operation(summary = "향수별 리뷰 목록 조회", description = "특정 향수에 대한 리뷰 목록을 조회합니다.")
    fun getReviewsByPerfumeId(
        @PathVariable("id") perfumeId: Long,
        @PageableDefault(size = 10, sort = ["createdAt"]) pageable: Pageable,
        @CurrentMember member: Member
    ): ApiResponse<Page<ReviewSummaryDto>> {
        val reviews = reviewService.getReviewsByPerfumeId(perfumeId, pageable, member.id)
        return ApiResponse.success(reviews)
    }

    @GetMapping("/member/{id}")
    @Operation(summary = "회원별 리뷰 목록 조회", description = "특정 회원이 작성한 리뷰 목록을 조회합니다.")
    fun getReviewsByMemberId(
        @PathVariable("id") memberId: Long,
        @PageableDefault(size = 10, sort = ["createdAt"]) pageable: Pageable
    ): ApiResponse<Page<ReviewSummaryDto>> {
        val reviews = reviewService.getReviewsByMemberId(memberId, pageable)
        return ApiResponse.success(reviews)
    }

    @GetMapping("/me")
    @Operation(summary = "내 리뷰 목록 조회", description = "현재 로그인한 회원이 작성한 리뷰 목록을 조회합니다.")
    fun getMyReviews(
        @CurrentMember member: Member,
        @PageableDefault(size = 10, sort = ["createdAt"]) pageable: Pageable
    ): ApiResponse<Page<ReviewSummaryDto>> {
        val reviews = reviewService.getReviewsByMemberId(member.id!!, pageable)
        return ApiResponse.success(reviews)
    }
} 