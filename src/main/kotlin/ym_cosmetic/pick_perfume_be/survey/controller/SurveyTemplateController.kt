package ym_cosmetic.pick_perfume_be.survey.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ym_cosmetic.pick_perfume_be.common.dto.response.ApiResponse
import ym_cosmetic.pick_perfume_be.survey.dto.SurveyTemplateResponse
import ym_cosmetic.pick_perfume_be.survey.entity.QuestionType
import ym_cosmetic.pick_perfume_be.survey.service.SurveyTemplateService

@RestController
@RequestMapping("/api/survey-templates")
@Tag(name = "설문 템플릿 API", description = "설문 템플릿 관리 API")
class SurveyTemplateController(
    private val surveyTemplateService: SurveyTemplateService
) {

    @GetMapping
    @Operation(summary = "모든 설문 템플릿 조회", description = "모든 설문 템플릿을 정렬 순서에 따라 조회합니다.")
    fun getAllTemplates(): ApiResponse<List<SurveyTemplateResponse>> {
        val templates = surveyTemplateService.getAllTemplates()
        return ApiResponse.success(templates)
    }

    @GetMapping("/{id}")
    @Operation(summary = "설문 템플릿 ID로 조회", description = "설문 템플릿을 ID로 조회합니다.")
    fun getTemplateById(@PathVariable id: Long): ApiResponse<SurveyTemplateResponse> {
        val template = surveyTemplateService.getTemplateById(id)
        return ApiResponse.success(template)
    }

    @GetMapping("/key/{key}")
    @Operation(summary = "설문 템플릿 키로 조회", description = "설문 템플릿을 키로 조회합니다.")
    fun getTemplateByKey(@PathVariable key: String): ApiResponse<SurveyTemplateResponse> {
        val template = surveyTemplateService.getTemplateByKey(key)
        return ApiResponse.success(template)
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "질문 유형별 설문 템플릿 조회", description = "특정 유형의 설문 템플릿을 조회합니다.")
    fun getTemplatesByType(@PathVariable type: QuestionType): ApiResponse<List<SurveyTemplateResponse>> {
        val templates = surveyTemplateService.getTemplatesByType(type)
        return ApiResponse.success(templates)
    }


}