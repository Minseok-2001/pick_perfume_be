package ym_cosmetic.pick_perfume_be.community.dto.response

import ym_cosmetic.pick_perfume_be.community.entity.Report
import ym_cosmetic.pick_perfume_be.community.enums.ReportStatus
import ym_cosmetic.pick_perfume_be.community.enums.ReportTargetType
import ym_cosmetic.pick_perfume_be.community.enums.ReportType
import java.time.LocalDateTime

data class ReportResponse(
    val id: Long,
    val reporterNickname: String,
    val reporterId: Long,
    val reportType: ReportType,
    val targetType: ReportTargetType,
    val targetId: Long,
    val content: String?,
    val status: ReportStatus,
    val processorNickname: String?,
    val processorId: Long?,
    val processorComment: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(report: Report): ReportResponse {
            return ReportResponse(
                id = report.id,
                reporterNickname = report.getReporter().nickname,
                reporterId = report.getReporter().id!!,
                reportType = report.getReportType(),
                targetType = report.getTargetType(),
                targetId = report.getTargetId(),
                content = report.getContent(),
                status = report.getStatus(),
                processorNickname = report.getProcessor()?.nickname,
                processorId = report.getProcessor()?.id,
                processorComment = report.getProcessorComment(),
                createdAt = report.createdAt,
                updatedAt = report.updatedAt
            )
        }
    }
} 