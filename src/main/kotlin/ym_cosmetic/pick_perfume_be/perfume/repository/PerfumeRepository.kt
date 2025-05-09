package ym_cosmetic.pick_perfume_be.perfume.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume


interface PerfumeRepository : JpaRepository<Perfume, Long> {
    fun findByNameContainingOrBrandContaining(
        name: String,
        brand: String,
        pageable: Pageable
    ): Page<Perfume>

    fun findByBrand(brand: String, pageable: Pageable): Page<Perfume>

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
