package ym_cosmetic.pick_perfume_be.batch.writer

import org.opensearch.data.core.OpenSearchOperations
import org.slf4j.LoggerFactory
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder
import org.springframework.stereotype.Component
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeRepository
import ym_cosmetic.pick_perfume_be.search.document.PerfumeDocument

@Component
class PerfumeIndexWriter(
    private val openSearchOperations: OpenSearchOperations,
    private val perfumeRepository: PerfumeRepository
) : ItemWriter<PerfumeDocument> {

    private val logger = LoggerFactory.getLogger(PerfumeIndexWriter::class.java)
    private val indexName = "perfumes"

    override fun write(chunk: Chunk<out PerfumeDocument>) {
        try {
            val documents = chunk.items
            logger.info("인덱싱할 향수 문서: ${documents.size}개")

            // 각 문서 개별 저장
            documents.forEach { document ->
                // 문서 ID 설정 (필수)
                val id = document.id.toString()

                // 인덱스 쿼리 생성
                val indexQuery = IndexQueryBuilder()
                    .withId(id)
                    .withObject(document)
                    .build()

                // 인덱싱 요청
                val result = openSearchOperations.index(
                    indexQuery,
                    IndexCoordinates.of(indexName)
                )

                logger.debug("문서 인덱싱 완료: ${document.name} (ID: $result)")

                // DB에 검색 동기화 상태 업데이트
                updatePerfumeSearchSyncStatus(document.id, true)
            }

            // 인덱스 리프레시 (즉시 검색 가능)
            openSearchOperations.indexOps(IndexCoordinates.of(indexName)).refresh()

        } catch (e: Exception) {
            logger.error("향수 인덱싱 중 오류 발생", e)
            throw e
        }
    }

    // 향수 검색 동기화 상태 업데이트
    private fun updatePerfumeSearchSyncStatus(perfumeId: Long, synced: Boolean) {
        try {
            perfumeRepository.findById(perfumeId).ifPresent { perfume ->
                perfume.setSearchSynced(synced)
                perfumeRepository.save(perfume)
            }
        } catch (e: Exception) {
            logger.error("향수 검색 동기화 상태 업데이트 중 오류 발생: 향수 ID $perfumeId", e)
        }
    }
} 