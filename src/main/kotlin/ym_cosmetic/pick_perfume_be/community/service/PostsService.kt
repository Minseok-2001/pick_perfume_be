package ym_cosmetic.pick_perfume_be.community.service

import jakarta.servlet.http.HttpServletRequest
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
import ym_cosmetic.pick_perfume_be.community.dto.response.RankingPostResponse
import ym_cosmetic.pick_perfume_be.community.entity.Post
import ym_cosmetic.pick_perfume_be.community.entity.PostLike
import ym_cosmetic.pick_perfume_be.community.entity.PostPerfumeEmbed
import ym_cosmetic.pick_perfume_be.community.entity.PostView
import ym_cosmetic.pick_perfume_be.community.enums.PeriodType
import ym_cosmetic.pick_perfume_be.community.enums.RankingType
import ym_cosmetic.pick_perfume_be.community.repository.*
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeRepository
import java.time.LocalDate
import java.time.LocalDateTime

@Service
@Transactional
class PostsService(
    private val postRepository: PostRepository,
    private val boardRepository: BoardRepository,
    private val perfumeRepository: PerfumeRepository,
    private val postLikeRepository: PostLikeRepository,
    private val commentRepository: CommentRepository,
    private val postPerfumeEmbedRepository: PostPerfumeEmbedRepository,
    private val postViewRepository: PostViewRepository
) {

    fun createPost(request: PostCreateRequest, member: Member): Long {
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

    /**
     * 게시물 상세 조회 (조회수 증가 X)
     */
    @Transactional(readOnly = true)
    fun getPostWithoutIncreasingViewCount(postId: Long, currentMember: Member?): PostResponse {
        val post = postRepository.findByIdAndIsDeletedFalse(postId)
            .orElseThrow { EntityNotFoundException("게시글을 찾을 수 없습니다.") }

        // 좋아요 수 조회
        val likeCount = postLikeRepository.countByPostId(postId)

        // 댓글 수 조회
        val commentCount = commentRepository.countByPostId(postId)

        // 조회수 조회 (PostView 테이블에서)
        val viewCount = postViewRepository.countByPostId(postId)

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
            isLikedByCurrentUser,
            viewCount
        )
    }

    /**
     * 게시물 상세 조회 (조회수 증가 O)
     */
    @Transactional
    fun getPost(postId: Long, currentMember: Member?, request: HttpServletRequest): PostResponse {
        val post = postRepository.findByIdAndIsDeletedFalse(postId)
            .orElseThrow { EntityNotFoundException("게시글을 찾을 수 없습니다.") }

        // 조회수 처리 - 하루에 한 번만 카운트
        increasePostViewCount(post, currentMember, request)

        // 좋아요 수 조회
        val likeCount = postLikeRepository.countByPostId(postId)

        // 댓글 수 조회
        val commentCount = commentRepository.countByPostId(postId)

        // 조회수 조회 (PostView 테이블에서)
        val viewCount = postViewRepository.countByPostId(postId)

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
            isLikedByCurrentUser,
            viewCount
        )
    }

    /**
     * 게시물 조회수만 증가
     */
    @Transactional
    fun increasePostViewCount(
        postId: Long,
        currentMember: Member?,
        request: HttpServletRequest
    ): Long {
        val post = postRepository.findByIdAndIsDeletedFalse(postId)
            .orElseThrow { EntityNotFoundException("게시글을 찾을 수 없습니다.") }

        return increasePostViewCount(post, currentMember, request)
    }

    /**
     * 게시물 조회수 증가 처리 (내부 메서드)
     */
    private fun increasePostViewCount(
        post: Post,
        currentMember: Member?,
        request: HttpServletRequest
    ): Long {
        val today = LocalDate.now()
        val ipAddress = extractClientIp(request)

        // 이미 조회했는지 확인
        val alreadyViewed = if (currentMember != null && currentMember.id != null) {
            // 로그인 사용자의 경우 회원 ID로 확인
            postViewRepository.existsByPostIdAndMemberIdAndViewDate(
                post.id,
                currentMember.id!!,
                today
            )
        } else if (ipAddress != null) {
            // 비로그인 사용자의 경우 IP 주소로 확인
            postViewRepository.existsByPostIdAndIpAddressAndViewDate(post.id, ipAddress, today)
        } else {
            false
        }

        // 아직 조회하지 않았으면 조회수 추가
        if (!alreadyViewed) {
            postViewRepository.save(PostView.create(post, currentMember, ipAddress))
        }

        return postViewRepository.countByPostId(post.id)
    }

    /**
     * 클라이언트 IP 주소 추출
     */
    private fun extractClientIp(request: HttpServletRequest): String? {
        var ip = request.getHeader("X-Forwarded-For")

        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("Proxy-Client-IP")
        }
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("WL-Proxy-Client-IP")
        }
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("HTTP_CLIENT_IP")
        }
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR")
        }
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.remoteAddr
        }

        // 여러 프록시를 거친 경우 첫 번째 IP만 사용
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim()
        }

        return ip
    }

    /**
     * 게시물 좋아요 토글
     */
    @Transactional
    fun toggleLike(postId: Long, member: Member): Boolean {
        val post = postRepository.findByIdAndIsDeletedFalse(postId)
            .orElseThrow { EntityNotFoundException("게시글을 찾을 수 없습니다.") }

        val existingLike = postLikeRepository.findByPostIdAndMemberId(postId, member.id!!)

        return if (existingLike != null) {
            // 이미 좋아요가 있으면 삭제 (좋아요 취소)
            postLikeRepository.delete(existingLike)
            false
        } else {
            // 좋아요가 없으면 추가
            val postLike = PostLike.create(post, member)
            postLikeRepository.save(postLike)
            true
        }
    }

    fun updatePost(postId: Long, request: PostUpdateRequest, member: Member): Long {
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

    fun deletePost(postId: Long, member: Member): Long {
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
    fun getPosts(
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
            val postViewCount = postViewRepository.countByPostId(post.id)

            // 썸네일용 향수 이미지 조회 (첫 번째 임베딩된 향수)
            val embeddedPerfumes = postPerfumeEmbedRepository.findByPostId(post.id)
            val thumbnailPerfume = embeddedPerfumes.firstOrNull()?.getPerfume()?.image?.url

            PostListResponse.from(
                post = post,
                likeCount = likeCount,
                commentCount = commentCount,
                thumbnailPerfume = thumbnailPerfume,
                isLikedByCurrentUser = post.id in likedPostIds,
                viewCount = postViewCount

            )
        }

        return PageResponse.from(postsPage.map { postListResponses.find { response -> response.id == it.id }!! })
    }

    @Transactional(readOnly = true)
    fun getPostsByBoard(
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
            val postViewCount = postViewRepository.countByPostId(post.id)

            // 썸네일용 향수 이미지 조회 (첫 번째 임베딩된 향수)
            val embeddedPerfumes = postPerfumeEmbedRepository.findByPostId(post.id)
            val thumbnailPerfume = embeddedPerfumes.firstOrNull()?.getPerfume()?.image?.url

            PostListResponse.from(
                post = post,
                likeCount = likeCount,
                commentCount = commentCount,
                thumbnailPerfume = thumbnailPerfume,
                isLikedByCurrentUser = post.id in likedPostIds,
                viewCount = postViewCount
            )
        }

        return PageResponse.from(postsPage.map { postListResponses.find { response -> response.id == it.id }!! })
    }

    @Transactional(readOnly = true)
    fun getPostsByMember(
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
            val postViewCount = postViewRepository.countByPostId(post.id)

            // 썸네일용 향수 이미지 조회 (첫 번째 임베딩된 향수)
            val embeddedPerfumes = postPerfumeEmbedRepository.findByPostId(post.id)
            val thumbnailPerfume = embeddedPerfumes.firstOrNull()?.getPerfume()?.image?.url

            PostListResponse.from(
                post = post,
                likeCount = likeCount,
                commentCount = commentCount,
                thumbnailPerfume = thumbnailPerfume,
                isLikedByCurrentUser = post.id in likedPostIds,
                viewCount = postViewCount
            )
        }

        return PageResponse.from(postsPage.map { postListResponses.find { response -> response.id == it.id }!! })
    }

    @Transactional(readOnly = true)
    fun searchPosts(
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
            val postViewCount = postViewRepository.countByPostId(post.id)

            // 썸네일용 향수 이미지 조회 (첫 번째 임베딩된 향수)
            val embeddedPerfumes = postPerfumeEmbedRepository.findByPostId(post.id)
            val thumbnailPerfume = embeddedPerfumes.firstOrNull()?.getPerfume()?.image?.url

            PostListResponse.from(
                post = post,
                likeCount = likeCount,
                commentCount = commentCount,
                thumbnailPerfume = thumbnailPerfume,
                isLikedByCurrentUser = post.id in likedPostIds,
                viewCount = postViewCount
            )
        }

        return PageResponse.from(postsPage.map { postListResponses.find { response -> response.id == it.id }!! })
    }

    /**
     * 랭킹 게시물 조회
     */
    @Transactional(readOnly = true)
    fun getRankingPosts(
        periodType: PeriodType,
        rankingType: RankingType,
        pageable: Pageable,
        boardId: Long?,
        currentMember: Member?
    ): List<RankingPostResponse> {
        val now = LocalDateTime.now()
        val startDate = periodType.period(now)

        // 랭킹 타입에 따른 정렬 기준 설정
        val rankByLikes = rankingType == RankingType.LIKES
        val rankByComments = rankingType == RankingType.COMMENTS

        val posts = postRepository.findRankingPosts(
            startDate = startDate,
            endDate = now,
            pageable = pageable,
            boardId = boardId,
            rankByLikes = rankByLikes,
            rankByComments = rankByComments
        )

        // 좋아요한 게시글 ID 목록 조회
        val likedPostIds = currentMember?.let {
            postLikeRepository.findPostIdsByMemberId(it.id!!).toSet()
        } ?: emptySet()

        // 응답 데이터 구성
        return posts.map { post ->
            val likeCount = postLikeRepository.countByPostId(post.id)
            val commentCount = commentRepository.countByPostId(post.id)
            val postViewCount = postViewRepository.countByPostId(post.id)

            // 썸네일용 향수 이미지 조회 (첫 번째 임베딩된 향수)
            val embeddedPerfumes = postPerfumeEmbedRepository.findByPostId(post.id)
            val thumbnailPerfume = embeddedPerfumes.firstOrNull()?.getPerfume()?.image?.url

            RankingPostResponse.from(
                post = post,
                likeCount = likeCount,
                commentCount = commentCount,
                thumbnailUrl = thumbnailPerfume,
                isLikedByCurrentUser = post.id in likedPostIds,
                viewCount = postViewCount
            )
        }
    }

    /**
     * 사용자가 좋아요한 게시물 목록 조회
     */
    @Transactional(readOnly = true)
    fun getLikedPosts(member: Member, pageable: Pageable): PageResponse<PostListResponse> {
        if (member.id == null) {
            throw IllegalArgumentException("인증된 사용자만 좋아요한 게시물 목록을 조회할 수 있습니다.")
        }

        // 사용자가 좋아요한 게시물 ID 목록 조회
        val likedPostIds = postLikeRepository.findPostIdsByMemberId(member.id!!)
        if (likedPostIds.isEmpty()) {
            return PageResponse.empty()
        }

        // 좋아요한 게시물 목록 조회
        val posts = postRepository.findByIdInAndIsDeletedFalse(likedPostIds, pageable)
        
        // 각 게시물의 좋아요 수, 댓글 수, 조회수 조회
        val postResponses = posts.content.map { post ->
            val likeCount = postLikeRepository.countByPostId(post.id)
            val commentCount = commentRepository.countByPostId(post.id)
            val viewCount = postViewRepository.countByPostId(post.id)
            
            // 썸네일 향수 이미지 URL 조회 (첫 번째 임베딩된 향수 이미지 사용)
            val thumbnailPerfume = postPerfumeEmbedRepository.findFirstByPostId(post.id)?.getPerfume()?.image?.url
            
            PostListResponse.from(
                post = post,
                likeCount = likeCount,
                commentCount = commentCount,
                thumbnailPerfume = thumbnailPerfume,
                isLikedByCurrentUser = true, // 본인이 좋아요한 목록이므로 항상 true
                viewCount = viewCount
            )
        }

        return PageResponse.from(
            posts.map { postResponses.find { response -> response.id == it.id }!! }
        )
    }

    /**
     * 사용자가 조회한 게시물 목록 조회
     */
    @Transactional(readOnly = true)
    fun getViewedPosts(member: Member, pageable: Pageable): PageResponse<PostListResponse> {
        if (member.id == null) {
            throw IllegalArgumentException("인증된 사용자만 조회한 게시물 목록을 조회할 수 있습니다.")
        }

        // 사용자가 조회한 게시물 ID 목록 조회
        val viewedPostIds = postViewRepository.findPostIdsByMemberId(member.id!!)
        if (viewedPostIds.isEmpty()) {
            return PageResponse.empty()
        }

        // 조회한 게시물 목록 조회
        val posts = postRepository.findByIdInAndIsDeletedFalse(viewedPostIds, pageable)
        
        // 사용자가 좋아요한 게시물 ID 목록 조회
        val likedPostIds = postLikeRepository.findPostIdsByMemberId(member.id!!)
        
        // 각 게시물의 좋아요 수, 댓글 수, 조회수 조회
        val postResponses = posts.content.map { post ->
            val likeCount = postLikeRepository.countByPostId(post.id)
            val commentCount = commentRepository.countByPostId(post.id)
            val viewCount = postViewRepository.countByPostId(post.id)
            
            // 썸네일 향수 이미지 URL 조회 (첫 번째 임베딩된 향수 이미지 사용)
            val thumbnailPerfume = postPerfumeEmbedRepository.findFirstByPostId(post.id)?.getPerfume()?.image?.url
            
            PostListResponse.from(
                post = post,
                likeCount = likeCount,
                commentCount = commentCount,
                thumbnailPerfume = thumbnailPerfume,
                isLikedByCurrentUser = likedPostIds.contains(post.id),
                viewCount = viewCount
            )
        }

        return PageResponse.from(
            posts.map { postResponses.find { response -> response.id == it.id }!! }
        )
    }
} 