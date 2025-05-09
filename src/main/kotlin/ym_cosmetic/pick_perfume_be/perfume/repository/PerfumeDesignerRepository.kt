package ym_cosmetic.pick_perfume_be.perfume.repository

import org.springframework.data.jpa.repository.JpaRepository
import ym_cosmetic.pick_perfume_be.perfume.entity.PerfumeDesigner

interface PerfumeDesignerRepository : JpaRepository<PerfumeDesigner, Long> {
    fun findPerfumeIdsByDesignerId(designerId: Long): List<Long>
    fun deleteByPerfumeId(perfumeId: Long)
}