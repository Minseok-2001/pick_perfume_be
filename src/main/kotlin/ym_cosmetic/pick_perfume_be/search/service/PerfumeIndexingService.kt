package ym_cosmetic.pick_perfume_be.search.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionalEventListener
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeRepository
import ym_cosmetic.pick_perfume_be.search.document.PerfumeDocument
import ym_cosmetic.pick_perfume_be.search.event.PerfumeCreatedEvent
import ym_cosmetic.pick_perfume_be.search.event.PerfumeUpdatedEvent
import ym_cosmetic.pick_perfume_be.search.mapper.PerfumeDocumentMapper
import ym_cosmetic.pick_perfume_be.search.repository.PerfumeSearchRepository

@Service
class PerfumeIndexingService(
    private val perfumeRepository: PerfumeRepository,
    private val perfumeSearchRepository: PerfumeSearchRepository,
    private val openSearchOperations: ElasticsearchOperations,
    private val perfumeDocumentMapper: PerfumeDocumentMapper
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val logger = LoggerFactory.getLogger(PerfumeIndexingService::class.java)

    // 비동기적으로 모든 향수 데이터 인덱싱
    fun indexAllPerfumes() {
        coroutineScope.launch {
            val perfumes = perfumeRepository.findAll()
            val documents = perfumes.map { perfumeDocumentMapper.toDocument(it) }

            documents.chunked(100).forEach { batch ->
                val queries = batch.map { document ->
                    IndexQueryBuilder()
                        .withId(document.id.toString())
                        .withObject(document)
                        .build()
                }

                openSearchOperations.bulkIndex(
                    queries,
                    IndexCoordinates.of("perfumes")
                )
            }
        }
    }

    // 향수 생성 이벤트 리스너
    @TransactionalEventListener
    fun handlePerfumeCreatedEvent(event: PerfumeCreatedEvent) {
        coroutineScope.launch {
            val perfume = perfumeRepository.findById(event.perfumeId).orElse(null) ?: return@launch
            val document = perfumeDocumentMapper.toDocument(perfume)
            perfumeSearchRepository.save(document)
        }
    }

    // 향수 업데이트 이벤트 리스너
    @TransactionalEventListener
    fun handlePerfumeUpdatedEvent(event: PerfumeUpdatedEvent) {
        coroutineScope.launch {
            val perfume = perfumeRepository.findById(event.perfumeId).orElse(null) ?: return@launch
            val document = perfumeDocumentMapper.toDocument(perfume)
            perfumeSearchRepository.save(document)
        }
    }

    // 단일 향수 인덱싱
    fun indexPerfume(perfumeId: Long) {
        coroutineScope.launch {
            val perfume = perfumeRepository.findById(perfumeId).orElse(null) ?: return@launch
            val document = perfumeDocumentMapper.toDocument(perfume)
            perfumeSearchRepository.save(document)
        }
    }

    @Transactional(readOnly = true)
    fun reindexAllPerfumes() {
        try {
            val perfumes = perfumeRepository.findAllWithDetails()
            val documents = perfumes.map { perfumeDocumentMapper.toDocument(it) }

            documents.chunked(100).forEach { batch ->
                val queries = batch.map { document ->
                    IndexQueryBuilder()
                        .withId(document.id.toString())
                        .withObject(document)
                        .build()
                }

                openSearchOperations.bulkIndex(
                    queries,
                    IndexCoordinates.of("perfumes")
                )
            }

            logger.info("향수 인덱싱 완료: ${documents.size}개 문서")
        } catch (e: Exception) {
            logger.error("인덱싱 중 오류 발생", e)
            throw e
        }
    }
    // 트랜잭션 내에서 데이터 로드
    @Transactional(readOnly = true)
    suspend fun loadPerfumeDocuments(): List<PerfumeDocument> {
        // FETCH JOIN으로 연관 엔티티를 함께 로드
        val perfumes = perfumeRepository.findAllWithDetails()
        return perfumes.map { perfumeDocumentMapper.toDocument(it) }
    }

    // 배치 인덱싱 처리
    private suspend fun indexBatch(batch: List<PerfumeDocument>) {
        val queries = batch.map { document ->
            IndexQueryBuilder()
                .withId(document.id.toString())
                .withObject(document)
                .build()
        }

        openSearchOperations.bulkIndex(
            queries,
            IndexCoordinates.of("perfumes")
        )
    }
}