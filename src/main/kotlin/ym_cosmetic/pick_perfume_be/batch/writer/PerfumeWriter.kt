package ym_cosmetic.pick_perfume_be.batch.writer

import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeRepository
import org.slf4j.LoggerFactory

@Component
class PerfumeWriter(
    private val perfumeRepository: PerfumeRepository
) : ItemWriter<Perfume> {

    private val logger = LoggerFactory.getLogger(PerfumeWriter::class.java)

    @Transactional
    override fun write(chunk: Chunk<out Perfume>) {
        try {
            val perfumes = chunk.items
            logger.info("저장할 향수 데이터: ${perfumes.size}개")
            
            // 일괄 저장
            perfumeRepository.saveAll(perfumes)
            
            // 향수 ID 로깅
            perfumes.forEach { perfume ->
                logger.debug("향수 저장 완료: ${perfume.name} (ID: ${perfume.id})")
            }
            
        } catch (e: Exception) {
            logger.error("향수 저장 중 오류 발생", e)
            throw e
        }
    }
} 