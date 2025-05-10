package ym_cosmetic.pick_perfume_be.batch.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ym_cosmetic.pick_perfume_be.batch.scheduler.BatchScheduler
import org.slf4j.LoggerFactory

@RestController
@RequestMapping("/api/admin/batch")
class BatchController(
    private val batchScheduler: BatchScheduler
) {
    private val logger = LoggerFactory.getLogger(BatchController::class.java)

    @PostMapping("/import-perfumes")
    fun importPerfumes(
        @RequestParam(required = false, defaultValue = "api") source: String
    ): ResponseEntity<String> {
        logger.info("향수 가져오기 작업 수동 실행 요청 받음 (소스: {})", source)
        val result = batchScheduler.runPerfumeImportJobManually(source)
        return ResponseEntity.ok(result)
    }
    
    @PostMapping("/index-perfumes")
    fun indexPerfumes(
        @RequestParam(required = false, defaultValue = "api") source: String
    ): ResponseEntity<String> {
        logger.info("향수 인덱싱 작업 수동 실행 요청 받음 (소스: {})", source)
        val result = batchScheduler.runPerfumeIndexJobManually(source)
        return ResponseEntity.ok(result)
    }
} 