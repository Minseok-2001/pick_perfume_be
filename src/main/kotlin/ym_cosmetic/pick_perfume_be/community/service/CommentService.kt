package ym_cosmetic.pick_perfume_be.community.service

import org.springframework.data.domain.Pageable
import ym_cosmetic.pick_perfume_be.community.dto.request.CommentCreateRequest
import ym_cosmetic.pick_perfume_be.community.dto.request.CommentUpdateRequest
import ym_cosmetic.pick_perfume_be.community.dto.response.CommentResponse
import ym_cosmetic.pick_perfume_be.community.dto.response.PageResponse
import ym_cosmetic.pick_perfume_be.member.entity.Member

interface CommentService {
    
    fun createComment(postId: Long, request: CommentCreateRequest, member: Member): Long
    
    fun getCommentsByPostId(postId: Long, pageable: Pageable, currentMember: Member?): PageResponse<CommentResponse>
    
    fun getCommentRepliesByParentId(parentId: Long, pageable: Pageable, currentMember: Member?): PageResponse<CommentResponse>
    
    fun updateComment(commentId: Long, request: CommentUpdateRequest, member: Member): Long
    
    fun deleteComment(commentId: Long, member: Member): Long
    
    fun likeComment(commentId: Long, member: Member): Long
    
    fun unlikeComment(commentId: Long, member: Member): Long
} 