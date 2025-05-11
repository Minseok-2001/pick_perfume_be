package ym_cosmetic.pick_perfume_be.survey.entity

enum class SurveyStatus {
    DRAFT,      // 임시 저장
    SUBMITTED,  // 제출됨
    PROCESSED   // 처리됨 (모델 분석 완료)
} 