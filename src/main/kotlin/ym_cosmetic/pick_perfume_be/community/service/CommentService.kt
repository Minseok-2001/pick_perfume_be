package ym_cosmetic.pick_perfume_be.community.service

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ym_cosmetic.pick_perfume_be.common.exception.EntityNotFoundException
import ym_cosmetic.pick_perfume_be.common.exception.UnauthorizedException
import ym_cosmetic.pick_perfume_be.community.dto.request.CommentCreateRequest
import ym_cosmetic.pick_perfume_be.community.dto.request.CommentUpdateRequest
import ym_cosmetic.pick_perfume_be.community.dto.response.CommentResponse
import ym_cosmetic.pick_perfume_be.community.dto.response.PageResponse
import ym_cosmetic.pick_perfume_be.community.entity.Comment
import ym_cosmetic.pick_perfume_be.community.entity.CommentLike
import ym_cosmetic.pick_perfume_be.community.repository.CommentLikeRepository
import ym_cosmetic.pick_perfume_be.community.repository.CommentRepository
import ym_cosmetic.pick_perfume_be.community.repository.PostRepository
import ym_cosmetic.pick_perfume_be.member.entity.Member

@Service
@Transactional
class CommentService(
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository,
    private val commentLikeRepository: CommentLikeRepository
)  {

   fun createComment(postId: Long, request: CommentCreateRequest, member: Member): Long {
        val post = postRepository.findByIdAndIsDeletedFalse(postId)
            .orElseThrow { EntityNotFoundException("게시글을 찾을 수 없습니다.") }

        val parentComment = request.parentId?.let { parentId ->
            commentRepository.findByIdAndIsDeletedFalse(parentId)
                .orElseThrow { EntityNotFoundException("부모 댓글을 찾을 수 없습니다.") }
        }

        val comment = Comment.create(
            content = request.content,
            post = post,
            member = member,
            parent = parentComment
        )

        parentComment?.addReply(comment)

        return commentRepository.save(comment).id
    }

    @Transactional(readOnly = true)
   fun getCommentsByPostId(
        postId: Long,
        pageable: Pageable,
        currentMember: Member?
    ): PageResponse<CommentResponse> {
        // 게시글 존재 확인
        postRepository.findByIdAndIsDeletedFalse(postId)
            .orElseThrow { EntityNotFoundException("게시글을 찾을 수 없습니다.") }

        // 좋아요한 댓글 ID 목록 조회
        val likedCommentIds = currentMember?.let {
            commentLikeRepository.findCommentIdsByMemberId(it.id!!).toSet()
        } ?: emptySet()

        // 루트 댓글 조회
        val commentsPage = commentRepository.findRootCommentsByPostId(postId, pageable)

        val commentResponses = commentsPage.map { comment ->
            val likeCount = commentLikeRepository.countByCommentId(comment.id)

            // 대댓글 조회 (첫 페이지만)
            val replies = comment.getReplies()
                .filter { !it.isDeleted() }
                .map { reply ->
                    val replyLikeCount = commentLikeRepository.countByCommentId(reply.id)
                    CommentResponse.from(
                        comment = reply,
                        likeCount = replyLikeCount,
                        isLikedByCurrentUser = reply.id in likedCommentIds
                    )
                }

            CommentResponse.from(
                comment = comment,
                likeCount = likeCount,
                isLikedByCurrentUser = comment.id in likedCommentIds,
                replies = replies
            )
        }

        return PageResponse.from(commentsPage.map { comment ->
            commentResponses.find { it.id == comment.id }!!
        })
    }

    @Transactional(readOnly = true)
   fun getCommentRepliesByParentId(
        parentId: Long,
        pageable: Pageable,
        currentMember: Member?
    ): PageResponse<CommentResponse> {
        // 부모 댓글 존재 확인
        commentRepository.findByIdAndIsDeletedFalse(parentId)
            .orElseThrow { EntityNotFoundException("댓글을 찾을 수 없습니다.") }

        // 좋아요한 댓글 ID 목록 조회
        val likedCommentIds = currentMember?.let {
            commentLikeRepository.findCommentIdsByMemberId(it.id!!).toSet()
        } ?: emptySet()

        // 대댓글 조회
        val repliesPage = commentRepository.findRepliesByParentId(parentId, pageable)

        val commentResponses = repliesPage.map { reply ->
            val likeCount = commentLikeRepository.countByCommentId(reply.id)

            CommentResponse.from(
                comment = reply,
                likeCount = likeCount,
                isLikedByCurrentUser = reply.id in likedCommentIds
            )
        }

        return PageResponse.from(repliesPage.map { reply ->
            commentResponses.find { it.id == reply.id }!!
        })
    }

   fun updateComment(
        commentId: Long,
        request: CommentUpdateRequest,
        member: Member
    ): Long {
        val comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
            .orElseThrow { EntityNotFoundException("댓글을 찾을 수 없습니다.") }

        // 댓글 작성자 확인
        if (comment.getMember().id!! != member.id!!) {
            throw UnauthorizedException("해당 댓글을 수정할 권한이 없습니다.")
        }

        comment.update(request.content)

        return comment.id
    }

   fun deleteComment(commentId: Long, member: Member): Long {
        val comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
            .orElseThrow { EntityNotFoundException("댓글을 찾을 수 없습니다.") }

        // 댓글 작성자 확인
        if (comment.getMember().id!! != member.id!!) {
            throw UnauthorizedException("해당 댓글을 삭제할 권한이 없습니다.")
        }

        comment.delete()

        return comment.id
    }

   fun likeComment(commentId: Long, member: Member): Long {
        val comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
            .orElseThrow { EntityNotFoundException("댓글을 찾을 수 없습니다.") }

        // 이미 좋아요한 경우 처리
        if (commentLikeRepository.existsByCommentIdAndMemberId(commentId, member.id!!)) {
            return commentId
        }

        val commentLike = CommentLike.create(comment, member)
        commentLikeRepository.save(commentLike)

        return commentId
    }

   fun unlikeComment(commentId: Long, member: Member): Long {
        commentRepository.findByIdAndIsDeletedFalse(commentId)
            .orElseThrow { EntityNotFoundException("댓글을 찾을 수 없습니다.") }

        commentLikeRepository.deleteByCommentIdAndMemberId(commentId, member.id!!)

        return commentId
    }
} 