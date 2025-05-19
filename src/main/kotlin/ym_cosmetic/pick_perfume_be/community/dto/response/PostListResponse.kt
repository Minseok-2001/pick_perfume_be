package ym_cosmetic.pick_perfume_be.community.dto.response

import ym_cosmetic.pick_perfume_be.community.entity.Post
import java.time.LocalDateTime

data class PostListResponse(
    val id: Long,
    val title: String,
    val board: BoardResponse,
    val createdAt: LocalDateTime,
    val viewCount: Long,
    val likeCount: Long,
    val commentCount: Long,
    val author: PostAuthorResponse,
    val thumbnailPerfume: String? = null,
    val isLikedByCurrentUser: Boolean = false
) {
    companion object {
        fun from(
            post: Post,
            likeCount: Long,
            commentCount: Long,
            thumbnailPerfume: String? = null,
            isLikedByCurrentUser: Boolean = false,
            viewCount: Long
        ): PostListResponse {
            return PostListResponse(
                id = post.id,
                title = post.getTitle(),
                board = BoardResponse.from(post.getBoard()),
                createdAt = post.createdAt,
                viewCount = viewCount,
                likeCount = likeCount,
                commentCount = commentCount,
                author = PostAuthorResponse.from(post.getMember()),
                thumbnailPerfume = thumbnailPerfume,
                isLikedByCurrentUser = isLikedByCurrentUser
            )
        }
    }
} 