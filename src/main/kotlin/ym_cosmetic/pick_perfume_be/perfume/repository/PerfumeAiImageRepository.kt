package ym_cosmetic.pick_perfume_be.perfume.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ym_cosmetic.pick_perfume_be.perfume.entity.PerfumeAiImage
import ym_cosmetic.pick_perfume_be.perfume.enums.PerfumeAiImagePromptType

@Repository
interface PerfumeAiImageRepository : JpaRepository<PerfumeAiImage, Long> {
    fun findByPerfumeId(perfumeId: Long): List<PerfumeAiImage>
    fun findByPerfumeIdAndPromptType(perfumeId: Long, promptType: PerfumeAiImagePromptType): PerfumeAiImage?
    fun countByPerfumeId(perfumeId: Long): Long
    fun deleteByPerfumeId(perfumeId: Long)
}
