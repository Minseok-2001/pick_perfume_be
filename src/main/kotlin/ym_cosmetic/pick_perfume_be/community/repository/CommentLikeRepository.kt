package ym_cosmetic.pick_perfume_be.community.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ym_cosmetic.pick_perfume_be.community.entity.CommentLike

@Repository
interface CommentLikeRepository : JpaRepository<CommentLike, Long> {
    
    fun existsByCommentIdAndMemberId(commentId: Long, memberId: Long): Boolean
    
    fun deleteByCommentIdAndMemberId(commentId: Long, memberId: Long)
    
    @Query("SELECT COUNT(cl) FROM CommentLike cl WHERE cl.comment.id = :commentId")
    fun countByCommentId(@Param("commentId") commentId: Long): Long
    
    @Query("SELECT cl.comment.id FROM CommentLike cl WHERE cl.member.id = :memberId")
    fun findCommentIdsByMemberId(@Param("memberId") memberId: Long): List<Long>
} 