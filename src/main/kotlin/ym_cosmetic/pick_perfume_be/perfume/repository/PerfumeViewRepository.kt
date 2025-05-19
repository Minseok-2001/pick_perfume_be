package ym_cosmetic.pick_perfume_be.perfume.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ym_cosmetic.pick_perfume_be.perfume.entity.PerfumeView
import java.time.LocalDate

interface PerfumeViewRepository : JpaRepository<PerfumeView, Long> {
    // 특정 향수의 총 조회수 집계
    @Query("SELECT COUNT(pv) FROM PerfumeView pv WHERE pv.perfume.id = :perfumeId")
    fun countByPerfumeId(perfumeId: Long): Int
    
    // 특정 날짜에 특정 사용자가 향수를 이미 조회했는지 확인
    fun existsByPerfumeIdAndMemberIdAndViewDate(
        perfumeId: Long, 
        memberId: Long, 
        viewDate: LocalDate
    ): Boolean
    
    // 특정 날짜에 특정 IP가 향수를 이미 조회했는지 확인
    fun existsByPerfumeIdAndIpAddressAndViewDate(
        perfumeId: Long, 
        ipAddress: String, 
        viewDate: LocalDate
    ): Boolean
    
    // 특정 날짜에 특정 향수의 조회수 집계
    fun countByPerfumeIdAndViewDate(perfumeId: Long, viewDate: LocalDate): Int
    
    // 특정 기간 내 향수 조회수 집계
    @Query("SELECT COUNT(pv) FROM PerfumeView pv WHERE pv.perfume.id = :perfumeId AND pv.viewDate BETWEEN :startDate AND :endDate")
    fun countByPerfumeIdAndViewDateBetween(
        perfumeId: Long, 
        startDate: LocalDate, 
        endDate: LocalDate
    ): Int
    
    // 여러 향수 ID들에 대한 조회수 집계
    @Query("SELECT pv.perfume.id, COUNT(pv) FROM PerfumeView pv WHERE pv.perfume.id IN :perfumeIds GROUP BY pv.perfume.id")
    fun countByPerfumeIdIn(perfumeIds: List<Long>): Map<Long, Long>
} 