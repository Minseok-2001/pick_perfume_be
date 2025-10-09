package ym_cosmetic.pick_perfume_be.perfume.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ym_cosmetic.pick_perfume_be.perfume.entity.PerfumeAiImageRequest
import ym_cosmetic.pick_perfume_be.perfume.enums.PerfumeAiImageProcessStatus
import ym_cosmetic.pick_perfume_be.perfume.enums.PerfumeAiImagePromptType

@Repository
interface PerfumeAiImageRequestRepository : JpaRepository<PerfumeAiImageRequest, Long> {
    fun existsByPerfumeIdAndPromptTypeAndStatusIn(
        perfumeId: Long,
        promptType: PerfumeAiImagePromptType,
        statuses: Collection<PerfumeAiImageProcessStatus>
    ): Boolean

    fun findTopByPerfumeIdAndPromptTypeOrderByCreatedAtDesc(
        perfumeId: Long,
        promptType: PerfumeAiImagePromptType
    ): PerfumeAiImageRequest?
}
