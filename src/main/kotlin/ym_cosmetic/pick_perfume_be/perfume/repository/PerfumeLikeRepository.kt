package ym_cosmetic.pick_perfume_be.perfume.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ym_cosmetic.pick_perfume_be.perfume.entity.PerfumeLike

interface PerfumeLikeRepository : JpaRepository<PerfumeLike, Long> {
    fun existsByPerfumeIdAndMemberId(perfumeId: Long, memberId: Long): Boolean

    @Query("SELECT pl.perfume.id FROM PerfumeLike pl WHERE pl.member.id = :memberId")
    fun findPerfumeIdsByMemberId(memberId: Long): Set<Long>
    
    fun findByPerfumeIdAndMemberId(perfumeId: Long, memberId: Long): PerfumeLike?
}