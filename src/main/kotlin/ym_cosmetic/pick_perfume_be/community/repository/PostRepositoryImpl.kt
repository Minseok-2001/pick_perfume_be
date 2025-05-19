package ym_cosmetic.pick_perfume_be.community.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Predicate
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import ym_cosmetic.pick_perfume_be.community.dto.PostSearchCondition
import ym_cosmetic.pick_perfume_be.community.entity.*
import ym_cosmetic.pick_perfume_be.member.entity.QMember
import ym_cosmetic.pick_perfume_be.perfume.entity.QPerfume
import java.time.LocalDateTime

@Repository
class PostRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : PostRepositoryCustom {

    private val post = QPost.post
    private val postLike = QPostLike.postLike
    private val comment = QComment.comment
    private val member = QMember.member
    private val board = QBoard.board
    private val perfumeEmbed = QPostPerfumeEmbed.postPerfumeEmbed
    private val perfume = QPerfume.perfume
    private val postView = QPostView.postView

    override fun searchPosts(condition: PostSearchCondition, pageable: Pageable): Page<Post> {
        val predicate = BooleanBuilder()
            .and(post.isDeleted.eq(false))
            .and(keywordContains(condition.keyword))
            .and(boardIdEq(condition.boardId))
            .and(memberIdEq(condition.memberId))
            .and(createdAtBetween(condition.startDate, condition.endDate))
            .and(hasPerfumeEmbed(condition.hasPerfumeEmbed, condition.perfumeId))

        val query = queryFactory
            .selectFrom(post)
            .leftJoin(post.member, member).fetchJoin()
            .leftJoin(post.board, board).fetchJoin()
            .leftJoin(perfumeEmbed).on(perfumeEmbed.post.eq(post))
            .leftJoin(perfumeEmbed.perfume, perfume)
            .where(predicate)
            .orderBy(post.createdAt.desc())
            .distinct()

        // Count 쿼리
        val totalCount = queryFactory
            .select(post.countDistinct())
            .from(post)
            .leftJoin(post.member, member)
            .leftJoin(post.board, board)
            .leftJoin(perfumeEmbed).on(perfumeEmbed.post.eq(post))
            .leftJoin(perfumeEmbed.perfume, perfume)
            .where(predicate)
            .fetchOne() ?: 0L

        // 페이징 적용
        val content = query
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        return PageImpl(content, pageable, totalCount)
    }

    override fun findRankingPosts(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        pageable: Pageable,
        boardId: Long?,
        rankByLikes: Boolean,
        rankByComments: Boolean
    ): List<Post> {
        // 공통 조건을 BooleanBuilder로 구성
        val whereConditions = BooleanBuilder()
            .and(post.createdAt.between(startDate, endDate))
            .and(post.isDeleted.eq(false))
        
        // boardId가 있으면 조건에 추가
        if (boardId != null) {
            whereConditions.and(post.board.id.eq(boardId))
        }
        
        // 정렬 기준에 따라 다른 쿼리 실행
        return when {
            rankByLikes -> {
                // 좋아요 수 기준 정렬
                queryFactory
                    .selectFrom(post)
                    .leftJoin(post.member).fetchJoin()
                    .leftJoin(postLike).on(postLike.post.eq(post))
                    .where(whereConditions)
                    .groupBy(post)
                    .orderBy(postLike.count().desc())
                    .offset(pageable.offset)
                    .limit(pageable.pageSize.toLong())
                    .fetch()
            }
            rankByComments -> {
                // 댓글 수 기준 정렬
                queryFactory
                    .selectFrom(post)
                    .leftJoin(post.member).fetchJoin()
                    .leftJoin(comment).on(comment.post.eq(post))
                    .where(whereConditions)
                    .groupBy(post)
                    .orderBy(comment.count().desc())
                    .offset(pageable.offset)
                    .limit(pageable.pageSize.toLong())
                    .fetch()
            }
            else -> {
                // 조회수 기준 정렬 (PostView 테이블 활용)
                queryFactory
                    .select(post)
                    .from(post)
                    .leftJoin(post.member).fetchJoin()
                    .leftJoin(postView).on(postView.post.eq(post))
                    .where(whereConditions)
                    .groupBy(post)
                    .orderBy(postView.count().desc(), post.createdAt.desc())
                    .offset(pageable.offset)
                    .limit(pageable.pageSize.toLong())
                    .fetch()
            }
        }
    }

    private fun keywordContains(keyword: String?): BooleanExpression? {
        if (keyword.isNullOrBlank()) return null

        return post.title.containsIgnoreCase(keyword)
            .or(post.content.containsIgnoreCase(keyword))
    }

    private fun boardIdEq(boardId: Long?): BooleanExpression? {
        if (boardId == null) return null

        return post.board.id.eq(boardId)
    }

    private fun memberIdEq(memberId: Long?): BooleanExpression? {
        if (memberId == null) return null

        return post.member.id.eq(memberId)
    }

    private fun createdAtBetween(
        startDate: LocalDateTime?,
        endDate: LocalDateTime?
    ): BooleanExpression? {
        if (startDate != null && endDate != null) {
            return post.createdAt.between(startDate, endDate)
        }

        if (startDate != null) {
            return post.createdAt.goe(startDate)
        }

        if (endDate != null) {
            return post.createdAt.loe(endDate)
        }

        return null
    }

    private fun hasPerfumeEmbed(hasPerfumeEmbed: Boolean?, perfumeId: Long?): BooleanExpression? {
        if (hasPerfumeEmbed == true) {
            val condition = post.id.eq(perfumeEmbed.post.id)

            if (perfumeId != null) {
                return condition.and(perfumeEmbed.perfume.id.eq(perfumeId))
            }

            return condition
        }

        return null
    }
} 