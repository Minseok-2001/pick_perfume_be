package ym_cosmetic.pick_perfume_be.vote.repository

import org.springframework.data.jpa.repository.JpaRepository
import ym_cosmetic.pick_perfume_be.vote.entity.PerfumeVoteStatistics

interface PerfumeVoteStatisticsRepository : JpaRepository<PerfumeVoteStatistics, Long> {
    fun findByPerfumeId(perfumeId: Long): PerfumeVoteStatistics?
}