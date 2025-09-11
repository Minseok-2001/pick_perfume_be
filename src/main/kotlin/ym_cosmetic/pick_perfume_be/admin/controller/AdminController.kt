package ym_cosmetic.pick_perfume_be.admin.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ym_cosmetic.pick_perfume_be.common.dto.response.ApiResponse
import ym_cosmetic.pick_perfume_be.search.service.PerfumeIndexingService

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val perfumeIndexingService: PerfumeIndexingService
) {
    
    /**
     * 모든 향수 데이터를 OpenSearch에 다시 인덱싱합니다.
     * 데이터가 손실되거나 인덱스를 재구성해야 할 때 사용합니다.
     */
    @PostMapping("/reindex-perfumes")
    fun reindexAllPerfumes(): ResponseEntity<ApiResponse<String>> {
        return try {
            perfumeIndexingService.reindexAllPerfumes()
            ResponseEntity.ok(
                ApiResponse.success("향수 데이터 리인덱싱이 시작되었습니다. 완료까지 수 분이 소요될 수 있습니다.")
            )
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(
                ApiResponse.error("리인덱싱 중 오류가 발생했습니다: ${e.message}")
            )
        }
    }
    
    /**
     * 특정 향수의 인덱스를 업데이트합니다.
     */
    @PostMapping("/reindex-perfume")
    fun reindexPerfume(perfumeId: Long): ResponseEntity<ApiResponse<String>> {
        return try {
            perfumeIndexingService.indexPerfume(perfumeId)
            ResponseEntity.ok(
                ApiResponse.success("향수 ID $perfumeId 의 인덱싱이 시작되었습니다.")
            )
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(
                ApiResponse.error("향수 인덱싱 중 오류가 발생했습니다: ${e.message}")
            )
        }
    }
}