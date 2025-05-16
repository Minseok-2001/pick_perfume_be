package ym_cosmetic.pick_perfume_be.community.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ym_cosmetic.pick_perfume_be.community.entity.PostLike

@Repository
interface PostLikeRepository : JpaRepository<PostLike, Long> {
    
    fun existsByPostIdAndMemberId(postId: Long, memberId: Long): Boolean
    
    fun deleteByPostIdAndMemberId(postId: Long, memberId: Long)
    
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.id = :postId")
    fun countByPostId(@Param("postId") postId: Long): Long
    
    @Query("SELECT pl.post.id FROM PostLike pl WHERE pl.member.id = :memberId")
    fun findPostIdsByMemberId(@Param("memberId") memberId: Long): List<Long>
} 