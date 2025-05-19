package ym_cosmetic.pick_perfume_be.perfume.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ym_cosmetic.pick_perfume_be.perfume.dto.request.PerfumeFilterRequest
import ym_cosmetic.pick_perfume_be.perfume.dto.response.AccordStat
import ym_cosmetic.pick_perfume_be.perfume.dto.response.BrandStat
import ym_cosmetic.pick_perfume_be.perfume.dto.response.GenderStat
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
    
    /**
     * 필터 조건에 맞는 향수 조회
     */
    fun findAllApprovedWithFilter(
        filter: PerfumeFilterRequest,
        pageable: Pageable
    ): Page<Perfume>
    
    /**
     * 브랜드별 향수 개수 통계 (상위 10개)
     */
    fun findTopBrandStats(limit: Int): List<BrandStat>
    
    /**
     * 성별별 향수 개수 통계
     */
    fun findGenderStats(): List<GenderStat>
    
    /**
     * 어코드별 향수 개수 통계 (상위 10개)
     */
    fun findTopAccordStats(limit: Int): List<AccordStat>
}