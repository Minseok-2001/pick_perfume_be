package ym_cosmetic.pick_perfume_be.recommendation.controller

import kotlinx.coroutines.runBlocking
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import ym_cosmetic.pick_perfume_be.common.dto.response.ApiResponse
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumeSummaryResponse
import ym_cosmetic.pick_perfume_be.recommendation.service.RecommendationService

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
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(defaultValue = "10") limit: Int
    ): ApiResponse<List<PerfumeSummaryResponse>> = runBlocking {
        val memberId = userDetails.username.toLong()
        val recommendations = recommendationService.getPersonalizedRecommendations(memberId, limit)
        ApiResponse.success(recommendations)
    }

    /**
     * 유사 향수 추천 목록 조회
     */
    @GetMapping("/similar/{perfumeId}")
    fun getSimilarPerfumes(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable perfumeId: Long,
        @RequestParam(defaultValue = "5") limit: Int
    ): ApiResponse<List<PerfumeSummaryResponse>> = runBlocking {
        val memberId = userDetails.username.toLong()
        val recommendations = recommendationService.getSimilarPerfumes(memberId, perfumeId, limit)
        ApiResponse.success(recommendations)
    }

    /**
     * 브랜드 기반 추천 목록 조회
     */
    @GetMapping("/by-brand/{brandName}")
    fun getRecommendationsByBrand(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable brandName: String,
        @RequestParam(defaultValue = "10") limit: Int
    ): ApiResponse<List<PerfumeSummaryResponse>> = runBlocking {
        val memberId = userDetails.username.toLong()
        val recommendations =
            recommendationService.getRecommendationsByBrand(memberId, brandName, limit)
        ApiResponse.success(recommendations)
    }

    /**
     * 노트 기반 추천 목록 조회
     */
    @GetMapping("/by-note/{noteName}")
    fun getRecommendationsByNote(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable noteName: String,
        @RequestParam(defaultValue = "10") limit: Int
    ): ApiResponse<List<PerfumeSummaryResponse>> = runBlocking {
        val memberId = userDetails.username.toLong()
        val recommendations =
            recommendationService.getRecommendationsByNote(memberId, noteName, limit)
        ApiResponse.success(recommendations)
    }

    /**
     * 하이브리드 추천 목록 조회
     */
    @GetMapping("/hybrid")
    fun getHybridRecommendations(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(defaultValue = "10") limit: Int
    ): ApiResponse<List<PerfumeSummaryResponse>> = runBlocking {
        val memberId = userDetails.username.toLong()
        val recommendations = recommendationService.getHybridRecommendations(memberId, limit)
        ApiResponse.success(recommendations)
    }

    /**
     * 추천 클릭 기록
     */
    @PostMapping("/click")
    fun recordRecommendationClick(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam perfumeId: Long,
        @RequestParam recommendationType: String
    ): ApiResponse<Unit> {
        val memberId = userDetails.username.toLong()
        recommendationService.recordRecommendationClick(memberId, perfumeId, recommendationType)
        return ApiResponse.success(Unit)
    }
}