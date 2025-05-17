package ym_cosmetic.pick_perfume_be.community.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*
import ym_cosmetic.pick_perfume_be.common.dto.response.ApiResponse
import ym_cosmetic.pick_perfume_be.community.dto.request.CommentCreateRequest
import ym_cosmetic.pick_perfume_be.community.dto.request.CommentUpdateRequest
import ym_cosmetic.pick_perfume_be.community.dto.response.CommentResponse
import ym_cosmetic.pick_perfume_be.community.dto.response.PageResponse
import ym_cosmetic.pick_perfume_be.community.service.CommentService
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.security.CurrentMember
import ym_cosmetic.pick_perfume_be.security.OptionalAuth

@RestController
@RequestMapping("/api/comments")
@Tag(name = "댓글 API", description = "댓글 관련 API")
class CommentController(
    private val commentService: CommentService
) {

    @PostMapping("/posts/{postId}/comments")
    @Operation(summary = "댓글 작성", description = "게시글에 새로운 댓글을 작성합니다.")
    fun createComment(
        @PathVariable postId: Long,
        @Valid @RequestBody request: CommentCreateRequest,
        @CurrentMember member: Member
    ): ApiResponse<Long> {
        val commentId = commentService.createComment(postId, request, member)
        return ApiResponse.success(commentId)
    }

    @GetMapping("/posts/{postId}/comments")
    @Operation(summary = "게시글 댓글 조회", description = "특정 게시글의 댓글 목록을 조회합니다.")
    fun getCommentsByPostId(
        @PathVariable postId: Long,
        @PageableDefault(
            size = 20,
            sort = ["createdAt"],
            direction = Sort.Direction.ASC
        ) pageable: Pageable,
        @CurrentMember @OptionalAuth member: Member?
    ): ApiResponse<Any> {
        val comments = commentService.getCommentsByPostId(postId, pageable, member)
        return ApiResponse.success(comments)
    }

    @GetMapping("/comments/{parentId}/replies")
    @Operation(summary = "대댓글 조회", description = "특정 댓글의 대댓글 목록을 조회합니다.")
    fun getCommentReplies(
        @PathVariable parentId: Long,
        @PageableDefault(
            size = 20,
            sort = ["createdAt"],
            direction = Sort.Direction.ASC
        ) pageable: Pageable,
        @CurrentMember @OptionalAuth member: Member?
    ): ApiResponse<PageResponse<CommentResponse>> {
        val replies = commentService.getCommentRepliesByParentId(parentId, pageable, member)
        return ApiResponse.success(replies)
    }

    @PutMapping("/comments/{commentId}")
    @Operation(summary = "댓글 수정", description = "특정 댓글을 수정합니다.")
    fun updateComment(
        @PathVariable commentId: Long,
        @Valid @RequestBody request: CommentUpdateRequest,
        @CurrentMember member: Member
    ): ApiResponse<Long> {
        val updatedCommentId = commentService.updateComment(commentId, request, member)
        return ApiResponse.success(updatedCommentId)
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "댓글 삭제", description = "특정 댓글을 삭제합니다.")
    fun deleteComment(
        @PathVariable commentId: Long,
        @CurrentMember member: Member
    ): ApiResponse<Long> {
        val deletedCommentId = commentService.deleteComment(commentId, member)
        return ApiResponse.success(deletedCommentId)
    }

    @PostMapping("/comments/{commentId}/like")
    @Operation(summary = "댓글 좋아요", description = "특정 댓글에 좋아요를 추가합니다.")
    fun likeComment(
        @PathVariable commentId: Long,
        @CurrentMember member: Member
    ): ApiResponse<Long> {
        val likedCommentId = commentService.likeComment(commentId, member)
        return ApiResponse.success(likedCommentId)
    }

    @DeleteMapping("/comments/{commentId}/like")
    @Operation(summary = "댓글 좋아요 취소", description = "특정 댓글의 좋아요를 취소합니다.")
    fun unlikeComment(
        @PathVariable commentId: Long,
        @CurrentMember member: Member
    ): ApiResponse<Long> {
        val unlikedCommentId = commentService.unlikeComment(commentId, member)
        return ApiResponse.success(unlikedCommentId)
    }
} 