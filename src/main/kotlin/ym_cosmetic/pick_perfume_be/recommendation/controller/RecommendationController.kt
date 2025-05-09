package ym_cosmetic.pick_perfume_be.recommendation.controller

import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.*
import ym_cosmetic.pick_perfume_be.common.dto.response.ApiResponse
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumeSummaryResponse
import ym_cosmetic.pick_perfume_be.recommendation.service.RecommendationService
import ym_cosmetic.pick_perfume_be.security.CurrentMember

@RestController
@RequestMapping("/recommendations")
class RecommendationController(
    private val recommendationService: RecommendationService
) {
    /**
     * 맞춤 추천 향수 목록 조회
     */
    @GetMapping("/personalized")
    fun getPersonalizedRecommendations(
        @CurrentMember member: Member,
        @RequestParam(defaultValue = "10") limit: Int
    ): ApiResponse<List<PerfumeSummaryResponse>> = runBlocking {
        val recommendations =
            recommendationService.getPersonalizedRecommendations(member.id!!, limit)
        ApiResponse.success(recommendations)
    }

    /**
     * 유사 향수 추천 목록 조회
     */
    @GetMapping("/similar/{perfumeId}")
    fun getSimilarPerfumes(
        @CurrentMember member: Member,
        @PathVariable perfumeId: Long,
        @RequestParam(defaultValue = "5") limit: Int
    ): ApiResponse<List<PerfumeSummaryResponse>> = runBlocking {
        val recommendations =
            recommendationService.getSimilarPerfumes(member.id!!, perfumeId, limit)
        ApiResponse.success(recommendations)
    }

    /**
     * 브랜드 기반 추천 목록 조회
     */
    @GetMapping("/by-brand/{brandName}")
    fun getRecommendationsByBrand(
        @CurrentMember member: Member,
        @PathVariable brandName: String,
        @RequestParam(defaultValue = "10") limit: Int
    ): ApiResponse<List<PerfumeSummaryResponse>> = runBlocking {
        val recommendations =
            recommendationService.getRecommendationsByBrand(member.id!!, brandName, limit)
        ApiResponse.success(recommendations)
    }

    /**
     * 노트 기반 추천 목록 조회
     */
    @GetMapping("/by-note/{noteName}")
    fun getRecommendationsByNote(
        @CurrentMember member: Member,
        @PathVariable noteName: String,
        @RequestParam(defaultValue = "10") limit: Int
    ): ApiResponse<List<PerfumeSummaryResponse>> = runBlocking {
        val recommendations =
            recommendationService.getRecommendationsByNote(member.id!!, noteName, limit)
        ApiResponse.success(recommendations)
    }

    /**
     * 하이브리드 추천 목록 조회
     */
    @GetMapping("/hybrid")
    fun getHybridRecommendations(
        @CurrentMember member: Member,
        @RequestParam(defaultValue = "10") limit: Int
    ): ApiResponse<List<PerfumeSummaryResponse>> = runBlocking {
        val recommendations = recommendationService.getHybridRecommendations(member.id!!, limit)
        ApiResponse.success(recommendations)
    }

    /**
     * 추천 클릭 기록
     */
    @PostMapping("/click")
    fun recordRecommendationClick(
        @CurrentMember member: Member,
        @RequestParam perfumeId: Long,
        @RequestParam recommendationType: String
    ): ApiResponse<Unit> {
        recommendationService.recordRecommendationClick(member.id!!, perfumeId, recommendationType)
        return ApiResponse.success(Unit)
    }
}