package ym_cosmetic.pick_perfume_be.community.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*
import ym_cosmetic.pick_perfume_be.common.dto.response.ApiResponse
import ym_cosmetic.pick_perfume_be.community.dto.PostSearchCondition
import ym_cosmetic.pick_perfume_be.community.dto.request.PostCreateRequest
import ym_cosmetic.pick_perfume_be.community.dto.request.PostUpdateRequest
import ym_cosmetic.pick_perfume_be.community.dto.response.PageResponse
import ym_cosmetic.pick_perfume_be.community.dto.response.PostListResponse
import ym_cosmetic.pick_perfume_be.community.dto.response.PostResponse
import ym_cosmetic.pick_perfume_be.community.dto.response.RankingPostResponse
import ym_cosmetic.pick_perfume_be.community.enums.PeriodType
import ym_cosmetic.pick_perfume_be.community.enums.RankingType
import ym_cosmetic.pick_perfume_be.community.service.PostsService
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.security.CurrentMember
import ym_cosmetic.pick_perfume_be.security.OptionalAuth

@RestController
@RequestMapping("/api/posts")
@Tag(name = "게시글 API", description = "게시글 관련 API")
class PostController(
    private val postService: PostsService
) {

    @PostMapping
    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다.")
    fun createPost(
        @Valid @RequestBody request: PostCreateRequest,
        @CurrentMember member: Member
    ): ApiResponse<Long> {
        val postId = postService.createPost(request, member)
        return ApiResponse.success("게시글이 등록되었습니다.", postId)

    }

    @GetMapping("/{postId}")
    @Operation(summary = "게시글 조회", description = "특정 게시글을 조회합니다.")
    fun getPost(
        @PathVariable postId: Long,
        @OptionalAuth @CurrentMember currentMember: Member?
    ): ApiResponse<PostResponse> {
        val post = postService.getPost(postId, currentMember)
        return ApiResponse.success("게시글 조회 성공", post)
    }


    @PutMapping("/{postId}")
    @Operation(summary = "게시글 수정", description = "특정 게시글을 수정합니다.")
    fun updatePost(
        @PathVariable postId: Long,
        @Valid @RequestBody request: PostUpdateRequest,
        @CurrentMember member: Member
    ): ApiResponse<Long> {
        val updatedPostId = postService.updatePost(postId, request, member)
        return ApiResponse.success("게시글이 수정되었습니다.", updatedPostId)

    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "게시글 삭제", description = "특정 게시글을 삭제합니다.")
    fun deletePost(
        @PathVariable postId: Long,
        @CurrentMember member: Member
    ): ApiResponse<Long> {
        val deletedPostId = postService.deletePost(postId, member)
        return ApiResponse.success("게시글이 삭제되었습니다.", deletedPostId)

    }

    @GetMapping
    @Operation(summary = "게시글 목록 조회", description = "게시글 목록을 페이징하여 조회합니다.")
    fun getPosts(
        @PageableDefault(
            size = 10,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC
        ) pageable: Pageable,
        @OptionalAuth @CurrentMember currentMember: Member?
    ): ApiResponse<PageResponse<PostListResponse>> {
        val posts = postService.getPosts(pageable, currentMember)
        return ApiResponse.success("게시글 목록 조회 성공", posts)
    }

    @GetMapping("/ranking")
    @Operation(
        summary = "랭킹 게시글 조회",
        description = "인기 게시글을 조회합니다. periodType: DAILY, WEEKLY, MONTHLY, rankingType: VIEWS, LIKES, COMMENTS"
    )
    fun getRankingPosts(
        @RequestParam(required = false, defaultValue = "WEEKLY") periodType: PeriodType,
        @RequestParam(required = false, defaultValue = "VIEWS") rankingType: RankingType,
        @RequestParam(required = false) boardId: Long?,
        @PageableDefault(size = 10) pageable: Pageable,
        @OptionalAuth @CurrentMember currentMember: Member?
    ): ApiResponse<List<RankingPostResponse>> {
        val posts =
            postService.getRankingPosts(periodType, rankingType, pageable, boardId, currentMember)
        return ApiResponse.success("랭킹 게시글 조회 성공", posts)

    }

    @GetMapping("/board/{boardId}")
    @Operation(summary = "게시판별 게시글 목록 조회", description = "특정 게시판의 게시글 목록을 페이징하여 조회합니다.")
    fun getPostsByBoard(
        @PathVariable boardId: Long,
        @PageableDefault(
            size = 10,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC
        ) pageable: Pageable,
        @OptionalAuth @CurrentMember currentMember: Member?
    ): ApiResponse<PageResponse<PostListResponse>> {
        val posts = postService.getPostsByBoard(boardId, pageable, currentMember)
        return ApiResponse.success("게시판별 게시글 목록 조회 성공", posts)

    }

    @GetMapping("/member/{memberId}")
    @Operation(summary = "사용자별 게시글 목록 조회", description = "특정 사용자의 게시글 목록을 페이징하여 조회합니다.")
    fun getPostsByMember(
        @PathVariable memberId: Long,
        @PageableDefault(
            size = 10,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC
        ) pageable: Pageable,
        @OptionalAuth @CurrentMember currentMember: Member?
    ): ApiResponse<PageResponse<PostListResponse>> {
        val posts = postService.getPostsByMember(memberId, pageable, currentMember)
        return ApiResponse.success("사용자별 게시글 목록 조회 성공", posts)
    }

    @GetMapping("/search")
    @Operation(summary = "게시글 검색", description = "조건에 맞는 게시글을 검색합니다.")
    fun searchPosts(
        @ModelAttribute condition: PostSearchCondition,
        @PageableDefault(
            size = 10,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC
        ) pageable: Pageable,
        @OptionalAuth @CurrentMember currentMember: Member?
    ): ApiResponse<PageResponse<PostListResponse>> {
        val posts = postService.searchPosts(condition, pageable, currentMember)
        return ApiResponse.success("게시글 검색 성공", posts)
    }

    @PostMapping("/{postId}/like")
    @Operation(summary = "게시글 좋아요", description = "특정 게시글에 좋아요를 추가합니다.")
    fun likePost(
        @PathVariable postId: Long,
        @CurrentMember member: Member
    ): ApiResponse<Boolean> {
        val isLiked = postService.toggleLike(postId, member)
        val message = if (isLiked) "게시글 좋아요 성공" else "게시글 좋아요 취소 성공"
        return ApiResponse.success(message, isLiked)
    }

    @DeleteMapping("/{postId}/like")
    @Operation(summary = "게시글 좋아요 취소", description = "특정 게시글에 좋아요를 취소합니다.")
    fun unlikePost(
        @PathVariable postId: Long,
        @CurrentMember member: Member
    ): ApiResponse<Boolean> {
        val isLiked = postService.toggleLike(postId, member)
        val message = if (isLiked) "게시글 좋아요 성공" else "게시글 좋아요 취소 성공"
        return ApiResponse.success(message, isLiked)
    }
} 