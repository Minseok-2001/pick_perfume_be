package ym_cosmetic.pick_perfume_be.perfume.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ym_cosmetic.pick_perfume_be.perfume.entity.PerfumeAiImageRequest
import ym_cosmetic.pick_perfume_be.perfume.enums.PerfumeAiImageProcessStatus

@Repository
interface PerfumeAiImageRequestRepository : JpaRepository<PerfumeAiImageRequest, Long> {
    fun existsByPerfumeIdAndStatusIn(perfumeId: Long, statuses: Collection<PerfumeAiImageProcessStatus>): Boolean
    fun findTopByPerfumeIdOrderByCreatedAtDesc(perfumeId: Long): PerfumeAiImageRequest?
}
