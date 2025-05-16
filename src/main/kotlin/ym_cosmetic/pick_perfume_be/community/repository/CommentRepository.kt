package ym_cosmetic.pick_perfume_be.community.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ym_cosmetic.pick_perfume_be.community.entity.Comment
import java.util.*

@Repository
interface CommentRepository : JpaRepository<Comment, Long> {
    
    @EntityGraph(attributePaths = ["member", "post"])
    fun findByIdAndIsDeletedFalse(id: Long): Optional<Comment>
    
    @Query("""
        SELECT c FROM Comment c 
        WHERE c.post.id = :postId AND c.parent IS NULL AND c.isDeleted = false 
        ORDER BY c.createdAt ASC
    """)
    @EntityGraph(attributePaths = ["member", "post"])
    fun findRootCommentsByPostId(@Param("postId") postId: Long, pageable: Pageable): Page<Comment>
    
    @Query("""
        SELECT c FROM Comment c 
        WHERE c.parent.id = :parentId AND c.isDeleted = false 
        ORDER BY c.createdAt ASC
    """)
    @EntityGraph(attributePaths = ["member", "post"])
    fun findRepliesByParentId(@Param("parentId") parentId: Long, pageable: Pageable): Page<Comment>
    
    @Query("""
        SELECT COUNT(c) FROM Comment c 
        WHERE c.post.id = :postId AND c.isDeleted = false
    """)
    fun countByPostId(@Param("postId") postId: Long): Long
    
    @Query("""
        SELECT c FROM Comment c 
        JOIN c.member m 
        WHERE m.id = :memberId AND c.isDeleted = false 
        ORDER BY c.createdAt DESC
    """)
    @EntityGraph(attributePaths = ["member", "post"])
    fun findByMemberId(@Param("memberId") memberId: Long, pageable: Pageable): Page<Comment>
} 