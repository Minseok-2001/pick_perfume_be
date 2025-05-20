package ym_cosmetic.pick_perfume_be.survey.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ym_cosmetic.pick_perfume_be.survey.entity.SurveyResponsePerfumeRating

@Repository
interface SurveyResponsePerfumeRatingRepository : JpaRepository<SurveyResponsePerfumeRating, Long> {

    /**
     * 응답 ID로 향수 평점 목록 조회
     */
    fun findByResponseId(responseId: Long): List<SurveyResponsePerfumeRating>

} 