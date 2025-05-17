package ym_cosmetic.pick_perfume_be.community.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ym_cosmetic.pick_perfume_be.community.entity.Report
import ym_cosmetic.pick_perfume_be.community.enums.ReportStatus
import ym_cosmetic.pick_perfume_be.community.enums.ReportTargetType
import java.util.*

@Repository
interface ReportRepository : JpaRepository<Report, Long> {
    
    @EntityGraph(attributePaths = ["reporter"])
    override fun findById(id: Long): Optional<Report>
    
    @EntityGraph(attributePaths = ["reporter", "processor"])
    override fun findAll(pageable: Pageable): Page<Report>
    
    @EntityGraph(attributePaths = ["reporter", "processor"])
    fun findByStatus(status: ReportStatus, pageable: Pageable): Page<Report>
    
    @EntityGraph(attributePaths = ["reporter"])
    fun findByTargetTypeAndTargetId(targetType: ReportTargetType, targetId: Long, pageable: Pageable): Page<Report>
    
    @Query("SELECT r FROM Report r WHERE r.reporter.id = :reporterId")
    @EntityGraph(attributePaths = ["reporter", "processor"])
    fun findByReporterId(@Param("reporterId") reporterId: Long, pageable: Pageable): Page<Report>
    
    @Query("SELECT COUNT(r) > 0 FROM Report r WHERE r.reporter.id = :reporterId AND r.targetType = :targetType AND r.targetId = :targetId")
    fun existsByReporterIdAndTargetTypeAndTargetId(
        @Param("reporterId") reporterId: Long,
        @Param("targetType") targetType: ReportTargetType,
        @Param("targetId") targetId: Long
    ): Boolean
} 