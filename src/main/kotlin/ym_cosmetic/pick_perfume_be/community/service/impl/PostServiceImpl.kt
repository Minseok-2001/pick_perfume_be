package ym_cosmetic.pick_perfume_be.community.service.impl

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ym_cosmetic.pick_perfume_be.common.exception.EntityNotFoundException
import ym_cosmetic.pick_perfume_be.common.exception.UnauthorizedException
import ym_cosmetic.pick_perfume_be.community.dto.PostSearchCondition
import ym_cosmetic.pick_perfume_be.community.dto.request.PostCreateRequest
import ym_cosmetic.pick_perfume_be.community.dto.request.PostUpdateRequest
import ym_cosmetic.pick_perfume_be.community.dto.response.PageResponse
import ym_cosmetic.pick_perfume_be.community.dto.response.PostListResponse
import ym_cosmetic.pick_perfume_be.community.dto.response.PostResponse
import ym_cosmetic.pick_perfume_be.community.entity.Post
import ym_cosmetic.pick_perfume_be.community.entity.PostLike
import ym_cosmetic.pick_perfume_be.community.entity.PostPerfumeEmbed
import ym_cosmetic.pick_perfume_be.community.repository.*
import ym_cosmetic.pick_perfume_be.community.service.PostsService
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeRepository
import java.time.LocalDateTime

@Service
@Transactional
class PostServiceImpl(
    private val postRepository: PostRepository,
    private val boardRepository: BoardRepository,
    private val perfumeRepository: PerfumeRepository,
    private val postLikeRepository: PostLikeRepository,
    private val commentRepository: CommentRepository,
    private val postPerfumeEmbedRepository: PostPerfumeEmbedRepository
) : PostsService {

    override fun createPost(request: PostCreateRequest, member: Member): Long {
        val board = boardRepository.findById(request.boardId)
            .orElseThrow { EntityNotFoundException("게시판을 찾을 수 없습니다.") }

        val post = Post.create(
            title = request.title,
            content = request.content,
            member = member,
            board = board
        )

        val savedPost = postRepository.save(post)

        // 향수 임베딩 처리
        request.perfumeIds?.forEach { perfumeId ->
            val perfume = perfumeRepository.findById(perfumeId)
                .orElseThrow { EntityNotFoundException("향수를 찾을 수 없습니다. ID: $perfumeId") }

            val embed = PostPerfumeEmbed.create(savedPost, perfume)
            postPerfumeEmbedRepository.save(embed)
        }

        return savedPost.id
    }

    @Transactional(readOnly = true)
    override fun getPost(postId: Long, currentMember: Member?): PostResponse {
        val post = postRepository.findByIdAndIsDeletedFalse(postId)
            .orElseThrow { EntityNotFoundException("게시글을 찾을 수 없습니다.") }

        // 조회수 증가
        post.incrementViewCount()
        postRepository.save(post)

        // 좋아요 수 조회
        val likeCount = postLikeRepository.countByPostId(postId)

        // 댓글 수 조회
        val commentCount = commentRepository.countByPostId(postId)

        // 임베딩된 향수 정보 조회
        val embeddedPerfumes = postPerfumeEmbedRepository.findByPostId(postId)

        // 현재 사용자의 좋아요 여부 확인
        val isLikedByCurrentUser = currentMember?.let {
            postLikeRepository.existsByPostIdAndMemberId(postId, it.id!!)
        } ?: false

        return PostResponse.from(
            post,
            likeCount,
            commentCount,
            embeddedPerfumes,
            isLikedByCurrentUser
        )
    }

    override fun updatePost(postId: Long, request: PostUpdateRequest, member: Member): Long {
        val post = postRepository.findByIdAndIsDeletedFalse(postId)
            .orElseThrow { EntityNotFoundException("게시글을 찾을 수 없습니다.") }

        // 글 작성자 확인
        if (post.getMember().id!! != member.id!!) {
            throw UnauthorizedException("해당 게시글을 수정할 권한이 없습니다.")
        }

        val board = boardRepository.findById(request.boardId)
            .orElseThrow { EntityNotFoundException("게시판을 찾을 수 없습니다.") }

        // 게시글 정보 업데이트
        post.update(request.title, request.content, board)

        // 향수 임베딩 업데이트
        if (request.perfumeIds != null) {
            // 기존 임베딩 삭제
            val currentEmbeds = postPerfumeEmbedRepository.findByPostId(postId)
            postPerfumeEmbedRepository.deleteAll(currentEmbeds)

            // 새 임베딩 추가
            request.perfumeIds.forEach { perfumeId ->
                val perfume = perfumeRepository.findById(perfumeId)
                    .orElseThrow { EntityNotFoundException("향수를 찾을 수 없습니다. ID: $perfumeId") }

                val embed = PostPerfumeEmbed.create(post, perfume)
                postPerfumeEmbedRepository.save(embed)
            }
        }

        return post.id
    }

    override fun deletePost(postId: Long, member: Member): Long {
        val post = postRepository.findByIdAndIsDeletedFalse(postId)
            .orElseThrow { EntityNotFoundException("게시글을 찾을 수 없습니다.") }

        // 글 작성자 확인
        if (post.getMember().id!! != member.id!!) {
            throw UnauthorizedException("해당 게시글을 삭제할 권한이 없습니다.")
        }

        post.delete()

        return post.id
    }

    @Transactional(readOnly = true)
    override fun getPosts(
        pageable: Pageable,
        currentMember: Member?
    ): PageResponse<PostListResponse> {
        val postsPage = postRepository.findByIsDeletedFalse(pageable)

        // 좋아요한 게시글 ID 목록 조회
        val likedPostIds = currentMember?.let {
            postLikeRepository.findPostIdsByMemberId(it.id!!).toSet()
        } ?: emptySet()

        val postListResponses = postsPage.map { post ->
            val likeCount = postLikeRepository.countByPostId(post.id)
            val commentCount = commentRepository.countByPostId(post.id)

            // 썸네일용 향수 이미지 조회 (첫 번째 임베딩된 향수)
            val embeddedPerfumes = postPerfumeEmbedRepository.findByPostId(post.id)
            val thumbnailPerfume = embeddedPerfumes.firstOrNull()?.getPerfume()?.image?.url

            PostListResponse.from(
                post = post,
                likeCount = likeCount,
                commentCount = commentCount,
                thumbnailPerfume = thumbnailPerfume,
                isLikedByCurrentUser = post.id in likedPostIds
            )
        }

        return PageResponse.from(postsPage.map { postListResponses.find { response -> response.id == it.id }!! })
    }

    @Transactional(readOnly = true)
    override fun getPostsByBoard(
        boardId: Long,
        pageable: Pageable,
        currentMember: Member?
    ): PageResponse<PostListResponse> {
        val postsPage = postRepository.findByBoardIdAndIsDeletedFalse(boardId, pageable)

        // 좋아요한 게시글 ID 목록 조회
        val likedPostIds = currentMember?.let {
            postLikeRepository.findPostIdsByMemberId(it.id!!).toSet()
        } ?: emptySet()

        val postListResponses = postsPage.map { post ->
            val likeCount = postLikeRepository.countByPostId(post.id)
            val commentCount = commentRepository.countByPostId(post.id)

            // 썸네일용 향수 이미지 조회 (첫 번째 임베딩된 향수)
            val embeddedPerfumes = postPerfumeEmbedRepository.findByPostId(post.id)
            val thumbnailPerfume = embeddedPerfumes.firstOrNull()?.getPerfume()?.image?.url

            PostListResponse.from(
                post = post,
                likeCount = likeCount,
                commentCount = commentCount,
                thumbnailPerfume = thumbnailPerfume,
                isLikedByCurrentUser = post.id in likedPostIds
            )
        }

        return PageResponse.from(postsPage.map { postListResponses.find { response -> response.id == it.id }!! })
    }

    @Transactional(readOnly = true)
    override fun getPostsByMember(
        memberId: Long,
        pageable: Pageable,
        currentMember: Member?
    ): PageResponse<PostListResponse> {
        val postsPage = postRepository.findByMemberIdAndIsDeletedFalse(memberId, pageable)

        // 좋아요한 게시글 ID 목록 조회
        val likedPostIds = currentMember?.let {
            postLikeRepository.findPostIdsByMemberId(it.id!!).toSet()
        } ?: emptySet()

        val postListResponses = postsPage.map { post ->
            val likeCount = postLikeRepository.countByPostId(post.id)
            val commentCount = commentRepository.countByPostId(post.id)

            // 썸네일용 향수 이미지 조회 (첫 번째 임베딩된 향수)
            val embeddedPerfumes = postPerfumeEmbedRepository.findByPostId(post.id)
            val thumbnailPerfume = embeddedPerfumes.firstOrNull()?.getPerfume()?.image?.url

            PostListResponse.from(
                post = post,
                likeCount = likeCount,
                commentCount = commentCount,
                thumbnailPerfume = thumbnailPerfume,
                isLikedByCurrentUser = post.id in likedPostIds
            )
        }

        return PageResponse.from(postsPage.map { postListResponses.find { response -> response.id == it.id }!! })
    }

    @Transactional(readOnly = true)
    override fun searchPosts(
        condition: PostSearchCondition,
        pageable: Pageable,
        currentMember: Member?
    ): PageResponse<PostListResponse> {
        val postsPage = postRepository.searchPosts(condition, pageable)

        // 좋아요한 게시글 ID 목록 조회
        val likedPostIds = currentMember?.let {
            postLikeRepository.findPostIdsByMemberId(it.id!!).toSet()
        } ?: emptySet()

        val postListResponses = postsPage.map { post ->
            val likeCount = postLikeRepository.countByPostId(post.id)
            val commentCount = commentRepository.countByPostId(post.id)

            // 썸네일용 향수 이미지 조회 (첫 번째 임베딩된 향수)
            val embeddedPerfumes = postPerfumeEmbedRepository.findByPostId(post.id)
            val thumbnailPerfume = embeddedPerfumes.firstOrNull()?.getPerfume()?.image?.url

            PostListResponse.from(
                post = post,
                likeCount = likeCount,
                commentCount = commentCount,
                thumbnailPerfume = thumbnailPerfume,
                isLikedByCurrentUser = post.id in likedPostIds
            )
        }

        return PageResponse.from(postsPage.map { postListResponses.find { response -> response.id == it.id }!! })
    }

    override fun likePost(postId: Long, member: Member): Long {
        val post = postRepository.findByIdAndIsDeletedFalse(postId)
            .orElseThrow { EntityNotFoundException("게시글을 찾을 수 없습니다.") }

        // 이미 좋아요한 경우 처리
        if (postLikeRepository.existsByPostIdAndMemberId(postId, member.id!!)) {
            return postId
        }

        val postLike = PostLike.create(post, member)
        postLikeRepository.save(postLike)

        return postId
    }

    override fun unlikePost(postId: Long, member: Member): Long {
        postRepository.findByIdAndIsDeletedFalse(postId)
            .orElseThrow { EntityNotFoundException("게시글을 찾을 수 없습니다.") }

        postLikeRepository.deleteByPostIdAndMemberId(postId, member.id!!)

        return postId
    }

    @Transactional(readOnly = true)
    override fun getTopPosts(
        boardId: Long?,
        timeRange: String,
        limit: Int,
        currentMember: Member?
    ): List<PostListResponse> {
        val now = LocalDateTime.now()
        val startDate = when (timeRange) {
            "daily" -> now.minusDays(1)
            "weekly" -> now.minusWeeks(1)
            "monthly" -> now.minusMonths(1)
            else -> now.minusWeeks(1) // 기본값은 주간
        }

        // 인기 게시글 조회 (조회수 순)
        val topPosts = postRepository.findTopPostsByViewCountAndTimeRange(
            startDate,
            now,
            Pageable.ofSize(limit)
        )

        // 좋아요한 게시글 ID 목록 조회
        val likedPostIds = currentMember?.let {
            postLikeRepository.findPostIdsByMemberId(it.id!!).toSet()
        } ?: emptySet()

        return topPosts
            .filter { boardId == null || it.getBoard().id == boardId }
            .map { post ->
                val likeCount = postLikeRepository.countByPostId(post.id)
                val commentCount = commentRepository.countByPostId(post.id)

                // 썸네일용 향수 이미지 조회 (첫 번째 임베딩된 향수)
                val embeddedPerfumes = postPerfumeEmbedRepository.findByPostId(post.id)
                val thumbnailPerfume = embeddedPerfumes.firstOrNull()?.getPerfume()?.image?.url

                PostListResponse.from(
                    post = post,
                    likeCount = likeCount,
                    commentCount = commentCount,
                    thumbnailPerfume = thumbnailPerfume,
                    isLikedByCurrentUser = post.id in likedPostIds
                )
            }
    }
} 