package ym_cosmetic.pick_perfume_be.survey.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ym_cosmetic.pick_perfume_be.survey.entity.Survey
import ym_cosmetic.pick_perfume_be.survey.entity.SurveyStatus
import java.time.LocalDateTime

@Repository
interface SurveyRepository : JpaRepository<Survey, Long> {

    /**
     * 회원 ID로 설문 목록 조회
     */
    fun findByMemberIdOrderByCreatedAtDesc(memberId: Long): List<Survey>

    /**
     * 상태별 설문 목록 조회
     */
    fun findByStatus(status: SurveyStatus): List<Survey>

    /**
     * 특정 기간에 생성된 설문 목록 조회
     */
    fun findByCreatedAtBetween(startDate: LocalDateTime, endDate: LocalDateTime): List<Survey>

    /**
     * 미처리 상태의 설문 수 조회
     */
    @Query("SELECT COUNT(s) FROM Survey s WHERE s.status = 'SUBMITTED'")
    fun countUnprocessedSurveys(): Long
} 