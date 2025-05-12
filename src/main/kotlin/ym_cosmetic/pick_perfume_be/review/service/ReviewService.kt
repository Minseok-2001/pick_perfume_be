package ym_cosmetic.pick_perfume_be.review.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ym_cosmetic.pick_perfume_be.common.exception.EntityNotFoundException
import ym_cosmetic.pick_perfume_be.common.exception.ForbiddenException
import ym_cosmetic.pick_perfume_be.member.repository.MemberRepository
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeRepository
import ym_cosmetic.pick_perfume_be.review.dto.*
import ym_cosmetic.pick_perfume_be.review.entity.Review
import ym_cosmetic.pick_perfume_be.review.repository.ReviewRepository
import ym_cosmetic.pick_perfume_be.review.vo.Rating

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val memberRepository: MemberRepository,
    private val perfumeRepository: PerfumeRepository,
    private val reviewReactionService: ReviewReactionService
) {

    /**
     * 리뷰 생성
     */
    @Transactional
    fun createReview(memberId: Long, request: ReviewCreateRequestDto): ReviewResponseDto {
        val member = memberRepository.findById(memberId)
            .orElseThrow { EntityNotFoundException("회원", memberId) }
        
        val perfume = perfumeRepository.findById(request.perfumeId)
            .orElseThrow { EntityNotFoundException("향수", request.perfumeId) }
        
        val review = Review(
            member = member,
            perfume = perfume,
            content = request.content,
            rating = Rating.of(request.rating),
            season = request.season,
            timeOfDay = request.timeOfDay,
            sentiment = request.sentiment,
        )
        
        val savedReview = reviewRepository.save(review)

        return ReviewResponseDto.from(
            review = savedReview,
            likeCount = 0,
            dislikeCount = 0,
            currentUserReaction = null
        )
    }

    /**
     * 리뷰 상세 조회
     */
    @Transactional(readOnly = true)
    fun getReviewById(reviewId: Long, currentUserId: Long?): ReviewResponseDto {
        val review = reviewRepository.findByIdWithMemberAndPerfume(reviewId)
            ?: throw EntityNotFoundException("리뷰", reviewId)
        
        val likeCount = reviewReactionService.countLikesByReviewId(reviewId)
        val dislikeCount = reviewReactionService.countDislikesByReviewId(reviewId)
        val currentUserReaction = if (currentUserId != null) {
            reviewReactionService.getUserReactionForReview(currentUserId, reviewId)
        } else {
            null
        }
        
        return ReviewResponseDto.from(
            review = review,
            likeCount = likeCount,
            dislikeCount = dislikeCount,
            currentUserReaction = currentUserReaction
        )
    }

    /**
     * 리뷰 수정
     */
    @Transactional
    fun updateReview(reviewId: Long, memberId: Long, request: ReviewUpdateRequestDto): ReviewResponseDto {
        val review = reviewRepository.findById(reviewId)
            .orElseThrow { EntityNotFoundException("리뷰", reviewId) }
        
        // 리뷰 작성자와 현재 사용자가 다른 경우 예외 처리
        if (review.member.id != memberId) {
            throw ForbiddenException("리뷰 수정 권한이 없습니다.")
        }
        
        review.update(
            content = request.content,
            rating = Rating.of(request.rating),
            season = request.season,
            timeOfDay = request.timeOfDay,
            sentiment = request.sentiment
        )
        
        val updatedReview = reviewRepository.save(review)
        
        val likeCount = reviewReactionService.countLikesByReviewId(reviewId)
        val dislikeCount = reviewReactionService.countDislikesByReviewId(reviewId)
        val userReaction = reviewReactionService.getUserReactionForReview(memberId, reviewId)
        
        return ReviewResponseDto.from(
            review = updatedReview,
            likeCount = likeCount,
            dislikeCount = dislikeCount,
            currentUserReaction = userReaction
        )
    }

    /**
     * 리뷰 삭제
     */
    @Transactional
    fun deleteReview(reviewId: Long, memberId: Long) {
        val review = reviewRepository.findById(reviewId)
            .orElseThrow { EntityNotFoundException("리뷰", reviewId) }
        
        // 리뷰 작성자와 현재 사용자가 다른 경우 예외 처리
        if (review.member.id != memberId) {
            throw ForbiddenException("리뷰 삭제 권한이 없습니다.")
        }
        
        reviewRepository.delete(review)
    }

    /**
     * 리뷰 검색
     */
    @Transactional(readOnly = true)
    fun searchReviews(searchDto: ReviewSearchDto, currentUserId: Long?): Page<ReviewSummaryDto> {
        val sort = when (searchDto.sortBy.lowercase()) {
            "rating" -> Sort.by(getSortDirection(searchDto.sortDirection), "rating.value")
            "likecount" -> Sort.by(getSortDirection(searchDto.sortDirection), "id") // 실제로는 더 복잡한 정렬 로직 필요
            else -> Sort.by(getSortDirection(searchDto.sortDirection), "createdAt")
        }
        
        val pageable: Pageable = PageRequest.of(searchDto.page, searchDto.size, sort)
        
        // TODO: 실제 구현에서는 QueryDSL 등을 사용하여 더 효율적인 검색 기능 구현
        val reviewPage = reviewRepository.findAll(pageable)
        
        return reviewPage.map { review ->
            val likeCount = reviewReactionService.countLikesByReviewId(review.id!!)
            ReviewSummaryDto.from(review, likeCount)
        }
    }

    /**
     * 특정 향수의 리뷰 목록 조회
     */
    @Transactional(readOnly = true)
    fun getReviewsByPerfumeId(perfumeId: Long, pageable: Pageable, currentUserId: Long?): Page<ReviewResponseDto> {
        val reviewPage = reviewRepository.findAll(pageable)
        
        return reviewPage.map { review ->
            reviewReactionService.countLikesByReviewId(review.id!!)
            ReviewResponseDto.from(
                review = review,
                likeCount = reviewReactionService.countLikesByReviewId(review.id!!),
                dislikeCount = reviewReactionService.countDislikesByReviewId(review.id!!),
                currentUserReaction = if (currentUserId != null) {
                    reviewReactionService.getUserReactionForReview(currentUserId, review.id!!)
                } else {
                    null
                }
            )
        }
    }

    /**
     * 특정 회원의 리뷰 목록 조회
     */
    @Transactional(readOnly = true)
    fun getReviewsByMemberId(memberId: Long, pageable: Pageable): Page<ReviewSummaryDto> {
        // TODO: memberId로 리뷰 목록 조회하는 Repository 메소드 구현 필요
        val reviewPage = reviewRepository.findAll(pageable)
        
        return reviewPage.map { review ->
            val likeCount = reviewReactionService.countLikesByReviewId(review.id!!)
            ReviewSummaryDto.from(review, likeCount)
        }
    }

    /**
     * 정렬 방향 결정
     */
    private fun getSortDirection(direction: String): Sort.Direction {
        return if (direction.equals("asc", ignoreCase = true)) {
            Sort.Direction.ASC
        } else {
            Sort.Direction.DESC
        }
    }
} 