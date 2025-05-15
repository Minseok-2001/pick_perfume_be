package ym_cosmetic.pick_perfume_be.perfume.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume

interface PerfumeRepositoryCustom {
    fun findByIdWithCreatorAndBrand(id: Long): Perfume?
    fun findAllApprovedWithCreatorAndBrand(pageable: Pageable): Page<Perfume>
    fun findByNameContainingOrBrandNameContaining(
        name: String,
        brandName: String,
        pageable: Pageable
    ): Page<Perfume>

    fun findByIdWithCreator(id: Long): Perfume?
    fun findAllApprovedWithCreator(pageable: Pageable): Page<Perfume>
    fun findRecentlyAdded(pageable: Pageable): Page<Perfume>
    fun findByBrandNameOrderByAverageRatingDesc(
        brandName: String,
        pageable: Pageable
    ): Page<Perfume>

    fun findTopByReviewCount(pageable: Pageable): Page<Perfume>
    fun findAllWithDetails(): List<Perfume>

    /**
     * 브랜드 정보를 포함한 향수 ID 목록으로 조회
     */
    fun findAllByIdsWithBrand(ids: List<Long>): List<Perfume>

}