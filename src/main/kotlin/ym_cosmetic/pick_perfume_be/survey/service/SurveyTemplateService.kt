package ym_cosmetic.pick_perfume_be.survey.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ym_cosmetic.pick_perfume_be.survey.dto.SurveyTemplateCreateRequest
import ym_cosmetic.pick_perfume_be.survey.dto.SurveyTemplateResponse
import ym_cosmetic.pick_perfume_be.survey.dto.SurveyTemplateUpdateRequest
import ym_cosmetic.pick_perfume_be.survey.entity.QuestionType
import ym_cosmetic.pick_perfume_be.survey.repository.SurveyTemplateRepository

@Service
class SurveyTemplateService(
    private val surveyTemplateRepository: SurveyTemplateRepository
) {

    /**
     * 모든 설문 템플릿 조회
     */
    @Transactional(readOnly = true)
    fun getAllTemplates(): List<SurveyTemplateResponse> {
        return surveyTemplateRepository.findAllByOrderBySortOrderAsc().map { SurveyTemplateResponse.fromEntity(it) }
    }

    /**
     * 설문 템플릿 ID로 조회
     */
    @Transactional(readOnly = true)
    fun getTemplateById(id: Long): SurveyTemplateResponse {
        val template = surveyTemplateRepository.findById(id)
            .orElseThrow { NoSuchElementException("설문 템플릿을 찾을 수 없습니다: $id") }
        return SurveyTemplateResponse.fromEntity(template)
    }

    /**
     * 설문 템플릿 키로 조회
     */
    @Transactional(readOnly = true)
    fun getTemplateByKey(key: String): SurveyTemplateResponse {
        val template = surveyTemplateRepository.findByQuestionKey(key)
            ?: throw NoSuchElementException("설문 템플릿을 찾을 수 없습니다: $key")
        return SurveyTemplateResponse.fromEntity(template)
    }

    /**
     * 질문 유형별 설문 템플릿 조회
     */
    @Transactional(readOnly = true)
    fun getTemplatesByType(type: QuestionType): List<SurveyTemplateResponse> {
        return surveyTemplateRepository.findByQuestionType(type).map { SurveyTemplateResponse.fromEntity(it) }
    }

    /**
     * 설문 템플릿 생성
     */
    @Transactional
    fun createTemplate(request: SurveyTemplateCreateRequest): SurveyTemplateResponse {
        // 질문 키 중복 검사
        surveyTemplateRepository.findByQuestionKey(request.questionKey)?.let {
            throw IllegalArgumentException("이미 존재하는 질문 키입니다: ${request.questionKey}")
        }
        
        // 질문 유형에 따른 유효성 검사
        validateTemplateRequest(request)
        
        // 템플릿 저장
        val template = surveyTemplateRepository.save(request.toEntity())
        return SurveyTemplateResponse.fromEntity(template)
    }

    /**
     * 설문 템플릿 삭제
     */
    @Transactional
    fun deleteTemplate(id: Long) {
        if (!surveyTemplateRepository.existsById(id)) {
            throw NoSuchElementException("설문 템플릿을 찾을 수 없습니다: $id")
        }
        surveyTemplateRepository.deleteById(id)
    }

    /**
     * 템플릿 생성 요청 유효성 검사
     */
    private fun validateTemplateRequest(request: SurveyTemplateCreateRequest) {
        when (request.questionType) {
            QuestionType.SINGLE_CHOICE, QuestionType.MULTIPLE_CHOICE -> {
                if (request.options.isNullOrEmpty()) {
                    throw IllegalArgumentException("선택형 질문에는 최소 하나 이상의 옵션이 필요합니다.")
                }
                if (request.questionType == QuestionType.MULTIPLE_CHOICE && request.maxSelections != null) {
                    if (request.maxSelections <= 0 || request.maxSelections > request.options.size) {
                        throw IllegalArgumentException("최대 선택 수는 1 이상 옵션 수 이하여야 합니다.")
                    }
                }
            }
            QuestionType.SLIDER -> {
                if (request.scale == null) {
                    throw IllegalArgumentException("슬라이더 질문에는 범위(scale)가 필요합니다.")
                }
                if (request.scale.min >= request.scale.max) {
                    throw IllegalArgumentException("슬라이더 최소값은 최대값보다 작아야 합니다.")
                }
            }
            QuestionType.MATRIX_SLIDER -> {
                if (request.options.isNullOrEmpty()) {
                    throw IllegalArgumentException("행렬 슬라이더 질문에는 항목(options)이 필요합니다.")
                }
                if (request.scale == null) {
                    throw IllegalArgumentException("행렬 슬라이더 질문에는 범위(scale)가 필요합니다.")
                }
                if (request.scale.min >= request.scale.max) {
                    throw IllegalArgumentException("슬라이더 최소값은 최대값보다 작아야 합니다.")
                }
            }
            QuestionType.NUMERIC_INPUT -> {
                if (request.scale == null) {
                    throw IllegalArgumentException("숫자 입력 질문에는 범위(scale)가 필요합니다.")
                }
                if (request.scale.min >= request.scale.max) {
                    throw IllegalArgumentException("숫자 입력 최소값은 최대값보다 작아야 합니다.")
                }
            }
            QuestionType.COLOR_PICKER -> {
                if (request.options.isNullOrEmpty()) {
                    throw IllegalArgumentException("색상 선택 질문에는 최소 하나 이상의 색상이 필요합니다.")
                }
            }
            QuestionType.PERFUME_RATING_SLIDER -> {
                if (request.scale == null) {
                    throw IllegalArgumentException("향수 평점 슬라이더 질문에는 범위(scale)가 필요합니다.")
                }
                if (request.scale.min >= request.scale.max) {
                    throw IllegalArgumentException("향수 평점 슬라이더 최소값은 최대값보다 작아야 합니다.")
                }
            }
        }
    }

    /**
     * 템플릿 수정 요청 유효성 검사
     */
    private fun validateTemplateUpdateRequest(request: SurveyTemplateUpdateRequest) {
        when (request.questionType) {
            QuestionType.SINGLE_CHOICE, QuestionType.MULTIPLE_CHOICE -> {
                if (request.options.isNullOrEmpty()) {
                    throw IllegalArgumentException("선택형 질문에는 최소 하나 이상의 옵션이 필요합니다.")
                }
                if (request.questionType == QuestionType.MULTIPLE_CHOICE && request.maxSelections != null) {
                    if (request.maxSelections <= 0 || request.maxSelections > request.options.size) {
                        throw IllegalArgumentException("최대 선택 수는 1 이상 옵션 수 이하여야 합니다.")
                    }
                }
            }
            QuestionType.SLIDER -> {
                if (request.scale == null) {
                    throw IllegalArgumentException("슬라이더 질문에는 범위(scale)가 필요합니다.")
                }
                if (request.scale.min >= request.scale.max) {
                    throw IllegalArgumentException("슬라이더 최소값은 최대값보다 작아야 합니다.")
                }
            }
            QuestionType.MATRIX_SLIDER -> {
                if (request.options.isNullOrEmpty()) {
                    throw IllegalArgumentException("행렬 슬라이더 질문에는 항목(options)이 필요합니다.")
                }
                if (request.scale == null) {
                    throw IllegalArgumentException("행렬 슬라이더 질문에는 범위(scale)가 필요합니다.")
                }
                if (request.scale.min >= request.scale.max) {
                    throw IllegalArgumentException("슬라이더 최소값은 최대값보다 작아야 합니다.")
                }
            }
            QuestionType.NUMERIC_INPUT -> {
                if (request.scale == null) {
                    throw IllegalArgumentException("숫자 입력 질문에는 범위(scale)가 필요합니다.")
                }
                if (request.scale.min >= request.scale.max) {
                    throw IllegalArgumentException("숫자 입력 최소값은 최대값보다 작아야 합니다.")
                }
            }
            QuestionType.COLOR_PICKER -> {
                if (request.options.isNullOrEmpty()) {
                    throw IllegalArgumentException("색상 선택 질문에는 최소 하나 이상의 색상이 필요합니다.")
                }
            }
            QuestionType.PERFUME_RATING_SLIDER -> {
                if (request.scale == null) {
                    throw IllegalArgumentException("향수 평점 슬라이더 질문에는 범위(scale)가 필요합니다.")
                }
                if (request.scale.min >= request.scale.max) {
                    throw IllegalArgumentException("향수 평점 슬라이더 최소값은 최대값보다 작아야 합니다.")
                }
            }
        }
    }
} 