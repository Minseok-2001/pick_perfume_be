package ym_cosmetic.pick_perfume_be.perfume.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume


interface PerfumeRepository : JpaRepository<Perfume, Long> {
    @Query(
        """
        SELECT DISTINCT p FROM Perfume p
        LEFT JOIN FETCH p.creator
        LEFT JOIN FETCH p.brand 
        WHERE p.id = :id
    """
    )
    fun findByIdWithCreatorAndBrand(@Param("id") id: Long): Perfume?

    @Query(
        """
        SELECT DISTINCT p FROM Perfume p
        LEFT JOIN FETCH p.creator
        LEFT JOIN FETCH p.brand 
        WHERE p.isApproved = true
    """
    )
    fun findAllApprovedWithCreatorAndBrand(pageable: Pageable): Page<Perfume>

    fun findByNameContainingOrBrandNameContaining(
        name: String,
        brandName: String,
        pageable: Pageable
    ): Page<Perfume>

    fun findByIdIn(ids: List<Long>, pageable: Pageable): Page<Perfume>

    fun findByNameContainingOrBrandContaining(
        name: String,
        brand: String,
        pageable: Pageable
    ): Page<Perfume>


    @Query(
        """
        SELECT DISTINCT p FROM Perfume p
        LEFT JOIN FETCH p.creator
        WHERE p.id = :id
    """
    )
    fun findByIdWithCreator(id: Long): Perfume?

    @Query(
        """
        SELECT DISTINCT p FROM Perfume p
        LEFT JOIN FETCH p.creator
        WHERE p.isApproved = true
    """
    )
    fun findAllApprovedWithCreator(pageable: Pageable): Page<Perfume>

    @Query(
        """
        SELECT p FROM Perfume p
        WHERE p.isApproved = true
        ORDER BY p.createdAt DESC
    """
    )
    fun findRecentlyAdded(pageable: Pageable): Page<Perfume>
}
