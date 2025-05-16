package ym_cosmetic.pick_perfume_be.community.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ym_cosmetic.pick_perfume_be.community.dto.PostSearchCondition
import ym_cosmetic.pick_perfume_be.community.entity.Post

interface PostRepositoryCustom {
    fun searchPosts(condition: PostSearchCondition, pageable: Pageable): Page<Post>
} 