package ym_cosmetic.pick_perfume_be.perfume.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import ym_cosmetic.pick_perfume_be.perfume.entity.PerfumeAccord

interface PerfumeAccordRepository : JpaRepository<PerfumeAccord, Long> {
    fun findByPerfumeId(perfumeId: Long): List<PerfumeAccord>

    @Modifying
    @Query("DELETE FROM PerfumeAccord pa WHERE pa.perfume.id = :perfumeId")
    fun deleteByPerfumeId(perfumeId: Long)
}