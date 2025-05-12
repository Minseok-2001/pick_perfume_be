package ym_cosmetic.pick_perfume_be.survey.entity

/**
 * 질문 유형
 */
enum class QuestionType {
    SINGLE_CHOICE,    // 단일 선택
    MULTIPLE_CHOICE,  // 다중 선택
    SLIDER,           // 슬라이더
    MATRIX_SLIDER,     // 행렬 슬라이더,
    NUMERIC_INPUT,    // 숫자 입력
    COLOR_PICKER,      // 색상 선택
    PERFUME_RATING_SLIDER, // 향수 평점 슬라이더
} 