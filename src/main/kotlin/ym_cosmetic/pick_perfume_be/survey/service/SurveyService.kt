package ym_cosmetic.pick_perfume_be.survey.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ym_cosmetic.pick_perfume_be.survey.dto.*
import ym_cosmetic.pick_perfume_be.survey.entity.Survey
import ym_cosmetic.pick_perfume_be.survey.entity.SurveyResponse
import ym_cosmetic.pick_perfume_be.survey.entity.SurveyStatus
import ym_cosmetic.pick_perfume_be.survey.repository.SurveyRepository
import ym_cosmetic.pick_perfume_be.survey.repository.SurveyResponseRepository
import ym_cosmetic.pick_perfume_be.survey.repository.SurveyTemplateRepository
import java.time.LocalDateTime
import java.util.*

@Service
class SurveyService(
    private val surveyRepository: SurveyRepository,
    private val surveyTemplateRepository: SurveyTemplateRepository,
    private val surveyResponseRepository: SurveyResponseRepository
) {

    /**
     * 설문 제출
     */
    @Transactional
    fun submitSurvey(request: SurveySubmitRequest): SurveyResponseDto {
        // 설문 엔티티 생성
        val survey = Survey(
            memberId = request.memberId,
            imageUrl = request.imageUrl,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            status = SurveyStatus.SUBMITTED
        )
        
        // 설문 저장
        val savedSurvey = surveyRepository.save(survey)
        
        // 응답 처리
        val responses = mutableListOf<SurveyAnswerDto>()
        
        for (responseDto in request.responses) {
            // 템플릿 조회
            val template = surveyTemplateRepository.findById(responseDto.questionId)
                .orElseThrow { NoSuchElementException("질문을 찾을 수 없습니다: ${responseDto.questionId}") }
            
            // 응답 유효성 검사
            if (!responseDto.validate(template)) {
                throw IllegalArgumentException("잘못된 응답 형식입니다: ${template.questionKey}")
            }
            
            // 응답 엔티티 생성
            val response = SurveyResponse(
                survey = savedSurvey,
                question = template,
                choiceAnswers = responseDto.choiceAnswers,
                sliderAnswer = responseDto.sliderAnswer,
                matrixAnswers = responseDto.matrixAnswers
            )
            
            // 응답 저장
            val savedResponse = surveyResponseRepository.save(response)
            savedSurvey.responses.add(savedResponse)
            
            // DTO 변환
            responses.add(SurveyAnswerDto.fromEntity(savedResponse))
        }
        
        return SurveyResponseDto.fromEntity(savedSurvey, responses)
    }

    /**
     * 설문 ID로 조회
     */
    @Transactional(readOnly = true)
    fun getSurveyById(id: Long): SurveyResponseDto {
        val survey = surveyRepository.findById(id)
            .orElseThrow { NoSuchElementException("설문을 찾을 수 없습니다: $id") }
        
        val responses = survey.responses.map { SurveyAnswerDto.fromEntity(it) }
        
        return SurveyResponseDto.fromEntity(survey, responses)
    }

    /**
     * 회원 ID로 설문 목록 조회
     */
    @Transactional(readOnly = true)
    fun getSurveysByMemberId(memberId: Long): List<SurveySummary> {
        return surveyRepository.findByMemberIdOrderByCreatedAtDesc(memberId)
            .map { SurveySummary.fromEntity(it) }
    }

    /**
     * 상태별 설문 목록 조회
     */
    @Transactional(readOnly = true)
    fun getSurveysByStatus(status: SurveyStatus): List<SurveySummary> {
        return surveyRepository.findByStatus(status)
            .map { SurveySummary.fromEntity(it) }
    }

    /**
     * 설문 상태 업데이트
     */
    @Transactional
    fun updateSurveyStatus(id: Long, status: SurveyStatus): SurveyResponseDto {
        val survey = surveyRepository.findById(id)
            .orElseThrow { NoSuchElementException("설문을 찾을 수 없습니다: $id") }
        
        // 새 설문 객체 생성 (상태 변경)
        val updatedSurvey = Survey(
            surveyId = survey.surveyId,
            memberId = survey.memberId,
            createdAt = survey.createdAt,
            updatedAt = LocalDateTime.now(),
            imageUrl = survey.imageUrl,
            status = status,
            responses = survey.responses
        )
        
        val savedSurvey = surveyRepository.save(updatedSurvey)
        val responses = savedSurvey.responses.map { SurveyAnswerDto.fromEntity(it) }
        
        return SurveyResponseDto.fromEntity(savedSurvey, responses)
    }

    /**
     * 설문 삭제
     */
    @Transactional
    fun deleteSurvey(id: Long) {
        if (!surveyRepository.existsById(id)) {
            throw NoSuchElementException("설문을 찾을 수 없습니다: $id")
        }
        surveyRepository.deleteById(id)
    }

    /**
     * 미처리 설문 수 조회
     */
    @Transactional(readOnly = true)
    fun getUnprocessedSurveyCount(): Long {
        return surveyRepository.countUnprocessedSurveys()
    }

    /**
     * 설문 분석 (머신러닝 모델 서빙을 위한 임시 메서드)
     */
    @Transactional
    fun analyzeSurvey(id: Long): SurveyAnalysisResult {
        val survey = surveyRepository.findById(id)
            .orElseThrow { NoSuchElementException("설문을 찾을 수 없습니다: $id") }
        
        // 실제 모델이 구현되면 해당 로직으로 대체
        // 현재는 임의의 추천 결과 반환
        val recommendations = listOf(
            PerfumeRecommendation(
                perfumeId = 1L,
                name = "샘플 향수 1",
                brand = "브랜드 A",
                imageUrl = "https://example.com/perfume1.jpg",
                score = 0.95,
                matchingFactors = listOf("우디", "오리엔탈", "따뜻함")
            ),
            PerfumeRecommendation(
                perfumeId = 2L,
                name = "샘플 향수 2",
                brand = "브랜드 B",
                imageUrl = "https://example.com/perfume2.jpg",
                score = 0.85,
                matchingFactors = listOf("플로럴", "과일향", "달콤함")
            )
        )
        
        // 설문 상태 업데이트
        updateSurveyStatus(id, SurveyStatus.PROCESSED)
        
        return SurveyAnalysisResult(
            surveyId = id,
            recommendations = recommendations
        )
    }
} 