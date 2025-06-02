package ym_cosmetic.pick_perfume_be.recommendation.controller

import org.springframework.web.bind.annotation.*
import ym_cosmetic.pick_perfume_be.common.dto.response.ApiResponse
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.recommendation.dto.request.SurveyRecommendationFeedbackRequest
import ym_cosmetic.pick_perfume_be.recommendation.dto.response.SurveyRecommendationResponse
import ym_cosmetic.pick_perfume_be.recommendation.service.SurveyRecommendationService
import ym_cosmetic.pick_perfume_be.security.CurrentMember
import ym_cosmetic.pick_perfume_be.security.OptionalAuth

@RestController
@RequestMapping("/api/recommendations/surveys")
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
        @PathVariable surveyId: Long?,
        @RequestParam(defaultValue = "5") limit: Int
    ): ApiResponse<List<SurveyRecommendationResponse>> {
        val recommendations = surveyRecommendationService.getRecommendationsBySurvey(surveyId, limit)
        return ApiResponse.success(recommendations)
    }

    /**
     * 설문 추천 결과 조회 API (피드백 포함)
     * 저장된 추천 결과를 조회합니다.
     *
     * @param surveyId 설문 ID
     * @param member 현재 로그인한 회원 (선택사항)
     * @return 추천 결과 목록
     */
    @GetMapping("/{surveyId}/results")
    fun getRecommendationsWithFeedback(
        @PathVariable surveyId: Long,
        @CurrentMember @OptionalAuth member: Member?
    ): ApiResponse<List<SurveyRecommendationResponse>> {
        val recommendations = surveyRecommendationService.getRecommendationsWithFeedback(
            surveyId, member?.id
        )
        return ApiResponse.success(recommendations)
    }

    /**
     * 설문 추천 결과 피드백 API
     * 사용자가 추천 결과에 대한 피드백을 제공합니다.
     *
     * @param surveyId 설문 ID
     * @param member 현재 로그인한 회원
     * @param feedbackRequests 피드백 요청 목록
     * @return 성공 메시지
     */
    @PostMapping("/{surveyId}/feedback")
    fun feedbackRecommendations(
        @PathVariable surveyId: Long,
        @CurrentMember @OptionalAuth member: Member?,
        @RequestBody feedbackRequests: List<SurveyRecommendationFeedbackRequest>
    ): ApiResponse<String> {
        surveyRecommendationService.feedbackRecommendations(surveyId, member, feedbackRequests)
        return ApiResponse.success("피드백이 성공적으로 저장되었습니다.")
    }
} 