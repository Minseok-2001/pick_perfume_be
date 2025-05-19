package ym_cosmetic.pick_perfume_be.community.dto.response

import ym_cosmetic.pick_perfume_be.community.entity.Post
import java.time.LocalDateTime

data class RankingPostResponse(
    val id: Long,
    val title: String,
    val memberNickname: String,
    val memberId: Long,
    val viewCount: Long,
    val likeCount: Long,
    val commentCount: Long,
    val createdAt: LocalDateTime,
    val thumbnailUrl: String?,
    val isLikedByCurrentUser: Boolean
) {
    companion object {
        fun from(
            post: Post,
            likeCount: Long,
            commentCount: Long,
            thumbnailUrl: String?,
            isLikedByCurrentUser: Boolean,
            viewCount: Long = 0
        ): RankingPostResponse {
            return RankingPostResponse(
                id = post.id,
                title = post.getTitle(),
                memberNickname = post.getMember().nickname,
                memberId = post.getMember().id!!,
                viewCount = viewCount,
                likeCount = likeCount,
                commentCount = commentCount,
                createdAt = post.createdAt,
                thumbnailUrl = thumbnailUrl,
                isLikedByCurrentUser = isLikedByCurrentUser
            )
        }
    }
} 