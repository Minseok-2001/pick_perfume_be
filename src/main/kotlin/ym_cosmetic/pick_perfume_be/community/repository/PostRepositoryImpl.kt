package ym_cosmetic.pick_perfume_be.community.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import ym_cosmetic.pick_perfume_be.community.dto.PostSearchCondition
import ym_cosmetic.pick_perfume_be.community.entity.Post
import ym_cosmetic.pick_perfume_be.community.entity.QBoard
import ym_cosmetic.pick_perfume_be.community.entity.QPost
import ym_cosmetic.pick_perfume_be.community.entity.QPostPerfumeEmbed
import ym_cosmetic.pick_perfume_be.member.entity.QMember
import ym_cosmetic.pick_perfume_be.perfume.entity.QPerfume
import java.time.LocalDateTime

@Repository
class PostRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : PostRepositoryCustom {

    override fun searchPosts(condition: PostSearchCondition, pageable: Pageable): Page<Post> {
        val post = QPost.post
        val member = QMember.member
        val board = QBoard.board
        val perfumeEmbed = QPostPerfumeEmbed.postPerfumeEmbed
        val perfume = QPerfume.perfume
        
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
    
    private fun keywordContains(keyword: String?): BooleanExpression? {
        if (keyword.isNullOrBlank()) return null
        
        val post = QPost.post
        return post.title.containsIgnoreCase(keyword)
            .or(post.content.containsIgnoreCase(keyword))
    }
    
    private fun boardIdEq(boardId: Long?): BooleanExpression? {
        if (boardId == null) return null
        
        return QPost.post.board.id.eq(boardId)
    }
    
    private fun memberIdEq(memberId: Long?): BooleanExpression? {
        if (memberId == null) return null
        
        return QPost.post.member.id.eq(memberId)
    }
    
    private fun createdAtBetween(startDate: LocalDateTime?, endDate: LocalDateTime?): BooleanExpression? {
        val post = QPost.post
        
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
        val postPerfumeEmbed = QPostPerfumeEmbed.postPerfumeEmbed
        
        if (hasPerfumeEmbed == true) {
            val condition = postPerfumeEmbed.post.id.eq(QPost.post.id)
            
            if (perfumeId != null) {
                return condition.and(postPerfumeEmbed.perfume.id.eq(perfumeId))
            }
            
            return condition
        }
        
        return null
    }
} 