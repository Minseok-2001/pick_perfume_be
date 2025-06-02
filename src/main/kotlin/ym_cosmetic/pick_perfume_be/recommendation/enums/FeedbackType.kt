package ym_cosmetic.pick_perfume_be.recommendation.enums

enum class FeedbackType(val description: String) {
    LIKE("좋아요"),
    DISLIKE("싫어요"),
    RATING("평점"),
    COMMENT("코멘트"),
    INTERESTED("관심있음"),
    NOT_INTERESTED("관심없음")
} 