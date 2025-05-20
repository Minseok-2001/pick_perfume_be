package ym_cosmetic.pick_perfume_be.survey.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import ym_cosmetic.pick_perfume_be.common.dto.response.ApiResponse
import ym_cosmetic.pick_perfume_be.survey.dto.SurveyAnalysisResult
import ym_cosmetic.pick_perfume_be.survey.dto.SurveyResponseDto
import ym_cosmetic.pick_perfume_be.survey.dto.SurveySubmitRequest
import ym_cosmetic.pick_perfume_be.survey.dto.SurveySummary
import ym_cosmetic.pick_perfume_be.survey.entity.SurveyStatus
import ym_cosmetic.pick_perfume_be.survey.service.SurveyService

@RestController
@RequestMapping("/api/surveys")
@Tag(name = "설문 API", description = "설문 제출 및 관리 API")
class SurveyController(
    private val surveyService: SurveyService
) {

    @PostMapping
    @Operation(
        summary = "설문 제출",
        description = "새로운 설문을 제출합니다. PERFUME_RATING_SLIDER 질문 유형의 경우, " +
                "향수 ID 또는 사용자가 직접 입력한 향수 이름을 함께 제출할 수 있습니다."
    )
    fun submitSurvey(@RequestBody request: SurveySubmitRequest): ApiResponse<SurveyResponseDto> {
        val survey = surveyService.submitSurvey(request)
        return ApiResponse.success(survey)
    }

    @GetMapping("/{id}")
    @Operation(summary = "설문 조회", description = "설문 ID로 설문 정보를 조회합니다. 향수 평점 정보도 함께 조회됩니다.")
    fun getSurveyById(@PathVariable id: Long): ApiResponse<SurveyResponseDto> {
        val survey = surveyService.getSurveyById(id)
        return ApiResponse.success(survey)
    }

    @GetMapping("/member/{memberId}")
    @Operation(summary = "회원별 설문 목록 조회", description = "특정 회원의 설문 목록을 조회합니다.")
    fun getSurveysByMemberId(@PathVariable memberId: Long): ApiResponse<List<SurveySummary>> {
        val surveys = surveyService.getSurveysByMemberId(memberId)
        return ApiResponse.success(surveys)
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "상태별 설문 목록 조회", description = "특정 상태의 설문 목록을 조회합니다.")
    fun getSurveysByStatus(@PathVariable status: SurveyStatus): ApiResponse<List<SurveySummary>> {
        val surveys = surveyService.getSurveysByStatus(status)
        return ApiResponse.success(surveys)
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "설문 상태 업데이트", description = "설문의 상태를 업데이트합니다.")
    fun updateSurveyStatus(
        @PathVariable id: Long,
        @RequestParam status: SurveyStatus
    ): ApiResponse<SurveyResponseDto> {
        val survey = surveyService.updateSurveyStatus(id, status)
        return ApiResponse.success(survey)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "설문 삭제", description = "설문을 삭제합니다.")
    fun deleteSurvey(@PathVariable id: Long): ApiResponse<Unit> {
        surveyService.deleteSurvey(id)
        return ApiResponse.success(Unit)
    }

    @GetMapping("/unprocessed/count")
    @Operation(summary = "미처리 설문 수 조회", description = "처리되지 않은 설문의 수를 조회합니다.")
    fun getUnprocessedSurveyCount(): ApiResponse<Long> {
        val count = surveyService.getUnprocessedSurveyCount()
        return ApiResponse.success(count)
    }

    @PostMapping("/{id}/analyze")
    @Operation(summary = "설문 분석", description = "설문을 분석하여 향수 추천 결과를 생성합니다.")
    fun analyzeSurvey(@PathVariable id: Long): ApiResponse<SurveyAnalysisResult> {
        val result = surveyService.analyzeSurvey(id)
        return ApiResponse.success(result)
    }
} 