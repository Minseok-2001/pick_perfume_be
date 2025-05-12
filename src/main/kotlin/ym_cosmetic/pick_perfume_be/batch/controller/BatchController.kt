package ym_cosmetic.pick_perfume_be.batch.controller

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ym_cosmetic.pick_perfume_be.batch.scheduler.BatchScheduler
import ym_cosmetic.pick_perfume_be.common.dto.response.ApiResponse
import ym_cosmetic.pick_perfume_be.search.service.PerfumeIndexingService

@RestController
@RequestMapping("/api/admin/batch")
class BatchController(
    private val batchScheduler: BatchScheduler,
    private val perfumeIndexingService: PerfumeIndexingService
) {
    private val logger = LoggerFactory.getLogger(BatchController::class.java)

    @PostMapping("/index-perfumes")
    fun indexPerfumes(): ApiResponse<String> {
        logger.info("향수 인덱싱 작업 수동 실행 요청 받음")
        try {
            perfumeIndexingService.reindexAllPerfumes()
            return ApiResponse.success("향수 ElasticSearch 인덱싱 작업이 시작되었습니다.")
        } catch (e: Exception) {
            logger.error("향수 ElasticSearch 인덱싱 작업 실패", e)
            return ApiResponse.error("향수 ElasticSearch 인덱싱 작업 실패: ${e.message}")
        }
    }
} 