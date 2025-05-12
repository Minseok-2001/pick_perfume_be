package ym_cosmetic.pick_perfume_be.vote.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import ym_cosmetic.pick_perfume_be.vote.entity.PerfumeVoteStatistics

interface PerfumeVoteStatisticsRepository : JpaRepository<PerfumeVoteStatistics, Long> {
    fun findByPerfumeId(perfumeId: Long): PerfumeVoteStatistics?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pvs FROM PerfumeVoteStatistics pvs WHERE pvs.perfume.id = :perfumeId")
    fun findByPerfumeIdForUpdate(perfumeId: Long): PerfumeVoteStatistics?
}