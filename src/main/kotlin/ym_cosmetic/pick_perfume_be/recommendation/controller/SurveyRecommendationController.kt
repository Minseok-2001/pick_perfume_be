package ym_cosmetic.pick_perfume_be.recommendation.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ym_cosmetic.pick_perfume_be.common.dto.response.ApiResponse
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumeSummaryResponse
import ym_cosmetic.pick_perfume_be.recommendation.service.SurveyRecommendationService

@RestController
@RequestMapping("/api/recommendations/survey")
class SurveyRecommendationController(
    private val surveyRecommendationService: SurveyRecommendationService
) {

    /**
     * 설문 기반 향수 추천 API
     * 사용자가 제출한 설문 데이터를 기반으로 맞춤형 향수를 추천합니다.
     *
     * @param surveyId 설문 ID
     * @param limit 추천 개수 (기본값: 5)
     * @return 추천된 향수 목록
     */
    @GetMapping("/{surveyId}")
    fun getRecommendationsBySurvey(
        @PathVariable surveyId: Long,
        @RequestParam(defaultValue = "5") limit: Int
    ): ApiResponse<List<PerfumeSummaryResponse>> {
        val recommendations = surveyRecommendationService.getRecommendationsBySurvey(surveyId, limit)
        return ApiResponse.success(recommendations)
    }
} 