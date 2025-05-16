package ym_cosmetic.pick_perfume_be.community.dto.response

import ym_cosmetic.pick_perfume_be.community.entity.Comment
import java.time.LocalDateTime

data class CommentResponse(
    val id: Long,
    val content: String,
    val author: PostAuthorResponse,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val likeCount: Long,
    val isLikedByCurrentUser: Boolean = false,
    val replies: List<CommentResponse> = emptyList(),
    val isDeleted: Boolean,
    val parentId: Long?
) {
    companion object {
        fun from(
            comment: Comment,
            likeCount: Long,
            isLikedByCurrentUser: Boolean = false,
            replies: List<CommentResponse> = emptyList()
        ): CommentResponse {
            return CommentResponse(
                id = comment.id,
                content = comment.getContent(),
                author = PostAuthorResponse.from(comment.getMember()),
                createdAt = comment.createdAt,
                updatedAt = comment.updatedAt,
                likeCount = likeCount,
                isLikedByCurrentUser = isLikedByCurrentUser,
                replies = replies,
                isDeleted = comment.isDeleted(),
                parentId = comment.getParent()?.id
            )
        }
    }
} 