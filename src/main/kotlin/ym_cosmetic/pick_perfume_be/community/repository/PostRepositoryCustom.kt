package ym_cosmetic.pick_perfume_be.community.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ym_cosmetic.pick_perfume_be.community.dto.PostSearchCondition
import ym_cosmetic.pick_perfume_be.community.entity.Post
import java.time.LocalDateTime

interface PostRepositoryCustom {
    fun searchPosts(condition: PostSearchCondition, pageable: Pageable): Page<Post>
    
    fun findRankingPosts(
        startDate: LocalDateTime, 
        endDate: LocalDateTime, 
        pageable: Pageable, 
        boardId: Long? = null,
        rankByLikes: Boolean = false, 
        rankByComments: Boolean = false
    ): List<Post>
} 