package ym_cosmetic.pick_perfume_be.review.dto

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import ym_cosmetic.pick_perfume_be.member.dto.MemberSummaryDto
import ym_cosmetic.pick_perfume_be.perfume.dto.PerfumeSummaryDto
import ym_cosmetic.pick_perfume_be.perfume.vo.Season
import ym_cosmetic.pick_perfume_be.review.entity.Review
import ym_cosmetic.pick_perfume_be.review.vo.Rating
import ym_cosmetic.pick_perfume_be.review.vo.Sentiment
import ym_cosmetic.pick_perfume_be.review.vo.TimeOfDay
import java.time.LocalDateTime

/**
 * 리뷰 조회 응답 DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ReviewResponseDto(
    val id: Long,
    val member: MemberSummaryDto,
    val perfume: PerfumeSummaryDto,
    val content: String,
    val rating: Int,
    val season: Season?,
    val timeOfDay: TimeOfDay?,
    val sentiment: Sentiment?,
    val likeCount: Long,
    val dislikeCount: Long,
    val currentUserReaction: Boolean?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(
            review: Review, 
            likeCount: Long, 
            dislikeCount: Long, 
            currentUserReaction: Boolean? = null
        ): ReviewResponseDto {
            return ReviewResponseDto(
                id = review.id!!,
                member = MemberSummaryDto.from(review.member),
                perfume = PerfumeSummaryDto.from(review.perfume),
                content = review.content,
                rating = review.rating.value,
                season = review.season,
                timeOfDay = review.timeOfDay,
                sentiment = review.sentiment,
                likeCount = likeCount,
                dislikeCount = dislikeCount,
                currentUserReaction = currentUserReaction,
                createdAt = review.createdAt,
                updatedAt = review.updatedAt
            )
        }
    }
}

/**
 * 리뷰 요약 DTO
 */
data class ReviewSummaryDto(
    val id: Long,
    val memberName: String,
    val perfumeName: String,
    val rating: Int,
    val content: String,
    val likeCount: Long,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(review: Review, likeCount: Long): ReviewSummaryDto {
            return ReviewSummaryDto(
                id = review.id!!,
                memberName = review.member.nickname ?: review.member.name,
                perfumeName = review.perfume.name,
                rating = review.rating.value,
                content = if (review.content.length > 100) review.content.substring(0, 97) + "..." else review.content,
                likeCount = likeCount,
                createdAt = review.createdAt
            )
        }
    }
}

/**
 * 리뷰 생성 요청 DTO
 */
data class ReviewCreateRequestDto(
    val perfumeId: Long,
    
    @field:NotBlank(message = "리뷰 내용은 필수입니다.")
    @field:Size(min = 10, max = 10000, message = "리뷰 내용은 10자 이상 10000자 이하로 작성해주세요.")
    val content: String,
    
    @field:Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
    @field:Max(value = 5, message = "평점은 5점 이하여야 합니다.")
    val rating: Int,
    
    val season: String? = null,
    val timeOfDay: String? = null,
    val sentiment: String? = null
)

/**
 * 리뷰 수정 요청 DTO
 */
data class ReviewUpdateRequestDto(
    @field:NotBlank(message = "리뷰 내용은 필수입니다.")
    @field:Size(min = 10, max = 10000, message = "리뷰 내용은 10자 이상 10000자 이하로 작성해주세요.")
    val content: String,
    
    @field:Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
    @field:Max(value = 5, message = "평점은 5점 이하여야 합니다.")
    val rating: Int,
    
    val season: String? = null,
    val timeOfDay: String? = null,
    val sentiment: String? = null
)

/**
 * 리뷰 리스트 검색 DTO
 */
data class ReviewSearchDto(
    val perfumeId: Long? = null,
    val memberId: Long? = null,
    val minRating: Int? = null,
    val season: String? = null,
    val timeOfDay: String? = null,
    val sentiment: String? = null,
    val sortBy: String = "createdAt", // createdAt, rating, likeCount
    val sortDirection: String = "DESC", // ASC, DESC
    val page: Int = 0,
    val size: Int = 10
) 