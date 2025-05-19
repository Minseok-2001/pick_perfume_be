package ym_cosmetic.pick_perfume_be.community.dto.response

import ym_cosmetic.pick_perfume_be.community.entity.Post
import ym_cosmetic.pick_perfume_be.community.entity.PostPerfumeEmbed
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumeSimpleResponse
import java.time.LocalDateTime

data class PostResponse(
    val id: Long,
    val title: String,
    val content: String,
    val board: BoardResponse,
    val viewCount: Long,
    val likeCount: Long,
    val commentCount: Long,
    val author: PostAuthorResponse,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val embeddedPerfumes: List<PerfumeSimpleResponse>,
    val isLikedByCurrentUser: Boolean = false
) {
    companion object {
        fun from(
            post: Post,
            likeCount: Long,
            commentCount: Long,
            embeddedPerfumes: List<PostPerfumeEmbed>,
            isLikedByCurrentUser: Boolean = false,
            viewCount: Long
        ): PostResponse {
            return PostResponse(
                id = post.id,
                title = post.getTitle(),
                content = post.getContent(),
                board = BoardResponse.from(post.getBoard()),
                viewCount = viewCount,
                likeCount = likeCount,
                commentCount = commentCount,
                author = PostAuthorResponse.from(post.getMember()),
                createdAt = post.createdAt,
                updatedAt = post.updatedAt,
                embeddedPerfumes = embeddedPerfumes.map { PerfumeSimpleResponse.from(it.getPerfume()) },
                isLikedByCurrentUser = isLikedByCurrentUser
            )
        }
    }
} 