package ym_cosmetic.pick_perfume_be.survey.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ym_cosmetic.pick_perfume_be.survey.dto.SurveyTemplateCreateRequest
import ym_cosmetic.pick_perfume_be.survey.dto.SurveyTemplateResponse
import ym_cosmetic.pick_perfume_be.survey.dto.SurveyTemplateUpdateRequest
import ym_cosmetic.pick_perfume_be.survey.entity.QuestionType
import ym_cosmetic.pick_perfume_be.survey.service.SurveyTemplateService

@RestController
@RequestMapping("/api/v1/survey-templates")
@Tag(name = "설문 템플릿 API", description = "설문 템플릿 관리 API")
class SurveyTemplateController(
    private val surveyTemplateService: SurveyTemplateService
) {

    @GetMapping
    @Operation(summary = "모든 설문 템플릿 조회", description = "모든 설문 템플릿을 정렬 순서에 따라 조회합니다.")
    fun getAllTemplates(): ResponseEntity<List<SurveyTemplateResponse>> {
        val templates = surveyTemplateService.getAllTemplates()
        return ResponseEntity.ok(templates)
    }

    @GetMapping("/{id}")
    @Operation(summary = "설문 템플릿 ID로 조회", description = "설문 템플릿을 ID로 조회합니다.")
    fun getTemplateById(@PathVariable id: Long): ResponseEntity<SurveyTemplateResponse> {
        val template = surveyTemplateService.getTemplateById(id)
        return ResponseEntity.ok(template)
    }

    @GetMapping("/key/{key}")
    @Operation(summary = "설문 템플릿 키로 조회", description = "설문 템플릿을 키로 조회합니다.")
    fun getTemplateByKey(@PathVariable key: String): ResponseEntity<SurveyTemplateResponse> {
        val template = surveyTemplateService.getTemplateByKey(key)
        return ResponseEntity.ok(template)
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "질문 유형별 설문 템플릿 조회", description = "특정 유형의 설문 템플릿을 조회합니다.")
    fun getTemplatesByType(@PathVariable type: QuestionType): ResponseEntity<List<SurveyTemplateResponse>> {
        val templates = surveyTemplateService.getTemplatesByType(type)
        return ResponseEntity.ok(templates)
    }

    @PostMapping
    @Operation(summary = "설문 템플릿 생성", description = "새로운 설문 템플릿을 생성합니다.")
    fun createTemplate(@RequestBody request: SurveyTemplateCreateRequest): ResponseEntity<SurveyTemplateResponse> {
        val createdTemplate = surveyTemplateService.createTemplate(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTemplate)
    }

    @PutMapping("/{id}")
    @Operation(summary = "설문 템플릿 수정", description = "기존 설문 템플릿을 수정합니다.")
    fun updateTemplate(
        @PathVariable id: Long,
        @RequestBody request: SurveyTemplateUpdateRequest
    ): ResponseEntity<SurveyTemplateResponse> {
        val updatedTemplate = surveyTemplateService.updateTemplate(id, request)
        return ResponseEntity.ok(updatedTemplate)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "설문 템플릿 삭제", description = "설문 템플릿을 삭제합니다.")
    fun deleteTemplate(@PathVariable id: Long): ResponseEntity<Unit> {
        surveyTemplateService.deleteTemplate(id)
        return ResponseEntity.noContent().build()
    }
} 