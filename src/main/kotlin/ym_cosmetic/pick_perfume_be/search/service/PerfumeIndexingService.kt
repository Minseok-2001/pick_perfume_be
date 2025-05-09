package ym_cosmetic.pick_perfume_be.search.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder
import org.springframework.stereotype.Service
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
    private val elasticsearchOperations: ElasticsearchOperations,
    private val perfumeDocumentMapper: PerfumeDocumentMapper
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // 비동기적으로 모든 향수 데이터 인덱싱
    fun indexAllPerfumes() {
        coroutineScope.launch {
            val perfumes = perfumeRepository.findAll()
            val documents = perfumes.map { perfumeDocumentMapper.toDocument(it) }

            documents.chunked(100).forEach { batch ->
                val queries = batch.map { document ->
                    IndexQueryBuilder()
                        .withId(document.id)
                        .withObject(document)
                        .build()
                }

                elasticsearchOperations.bulkIndex(
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

    // 인덱스 삭제 및 재생성
    fun reindexAllPerfumes() {
        coroutineScope.launch {
            elasticsearchOperations.indexOps(PerfumeDocument::class.java).delete()
            elasticsearchOperations.indexOps(PerfumeDocument::class.java).create()
            indexAllPerfumes()
        }
    }
}