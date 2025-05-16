package ym_cosmetic.pick_perfume_be.community.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ym_cosmetic.pick_perfume_be.community.entity.PostPerfumeEmbed

@Repository
interface PostPerfumeEmbedRepository : JpaRepository<PostPerfumeEmbed, Long> {
    
    @EntityGraph(attributePaths = ["post", "perfume"])
    fun findByPostId(postId: Long): List<PostPerfumeEmbed>
    
    @Query("SELECT DISTINCT pe.post.id FROM PostPerfumeEmbed pe WHERE pe.perfume.id = :perfumeId")
    fun findPostIdsByPerfumeId(@Param("perfumeId") perfumeId: Long): List<Long>
    
    fun existsByPostIdAndPerfumeId(postId: Long, perfumeId: Long): Boolean
    
    fun deleteByPostIdAndPerfumeId(postId: Long, perfumeId: Long)
} 