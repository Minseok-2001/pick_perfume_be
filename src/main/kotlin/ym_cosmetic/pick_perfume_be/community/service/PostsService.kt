package ym_cosmetic.pick_perfume_be.community.service

import org.springframework.data.domain.Pageable
import ym_cosmetic.pick_perfume_be.community.dto.PostSearchCondition
import ym_cosmetic.pick_perfume_be.community.dto.request.PostCreateRequest
import ym_cosmetic.pick_perfume_be.community.dto.request.PostUpdateRequest
import ym_cosmetic.pick_perfume_be.community.dto.response.PageResponse
import ym_cosmetic.pick_perfume_be.community.dto.response.PostListResponse
import ym_cosmetic.pick_perfume_be.community.dto.response.PostResponse
import ym_cosmetic.pick_perfume_be.member.entity.Member

interface PostsService {

    fun createPost(request: PostCreateRequest, member: Member): Long
    
    fun getPost(postId: Long, currentMember: Member?): PostResponse

    fun updatePost(postId: Long, request: PostUpdateRequest, member: Member): Long

    fun deletePost(postId: Long, member: Member): Long

    fun getPosts(pageable: Pageable, currentMember: Member?): PageResponse<PostListResponse>

    fun getPostsByBoard(
        boardId: Long,
        pageable: Pageable,
        currentMember: Member?
    ): PageResponse<PostListResponse>

    fun getPostsByMember(
        memberId: Long,
        pageable: Pageable,
        currentMember: Member?
    ): PageResponse<PostListResponse>

    fun searchPosts(
        condition: PostSearchCondition,
        pageable: Pageable,
        currentMember: Member?
    ): PageResponse<PostListResponse>

    fun likePost(postId: Long, member: Member): Long

    fun unlikePost(postId: Long, member: Member): Long

    fun getTopPosts(
        boardId: Long?,
        timeRange: String,
        limit: Int,
        currentMember: Member?
    ): List<PostListResponse>
} 