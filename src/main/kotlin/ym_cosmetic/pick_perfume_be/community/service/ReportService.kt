package ym_cosmetic.pick_perfume_be.community.service

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ym_cosmetic.pick_perfume_be.common.exception.DuplicateResourceException
import ym_cosmetic.pick_perfume_be.common.exception.EntityNotFoundException
import ym_cosmetic.pick_perfume_be.community.dto.request.ReportCreateRequest
import ym_cosmetic.pick_perfume_be.community.dto.request.ReportUpdateRequest
import ym_cosmetic.pick_perfume_be.community.dto.response.PageResponse
import ym_cosmetic.pick_perfume_be.community.dto.response.ReportResponse
import ym_cosmetic.pick_perfume_be.community.entity.Report
import ym_cosmetic.pick_perfume_be.community.enums.ReportStatus
import ym_cosmetic.pick_perfume_be.community.enums.ReportTargetType
import ym_cosmetic.pick_perfume_be.community.repository.CommentRepository
import ym_cosmetic.pick_perfume_be.community.repository.PostRepository
import ym_cosmetic.pick_perfume_be.community.repository.ReportRepository
import ym_cosmetic.pick_perfume_be.member.entity.Member

@Service
@Transactional
class ReportService(
    private val reportRepository: ReportRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository
)  {

    /**
     * 신고 생성
     */
     fun createReport(request: ReportCreateRequest, reporter: Member): Long {
        // 이미 신고한 경우 중복 신고 방지
        if (hasReported(reporter.id!!, request.targetType, request.targetId)) {
            throw DuplicateResourceException("이미 신고한 ${request.targetType.name.lowercase()}입니다.")
        }
        
        // 대상이 존재하는지 확인
        when (request.targetType) {
            ReportTargetType.POST -> {
                postRepository.findByIdAndIsDeletedFalse(request.targetId)
                    .orElseThrow { EntityNotFoundException("게시글을 찾을 수 없습니다.") }
            }
            ReportTargetType.COMMENT -> {
                commentRepository.findByIdAndIsDeletedFalse(request.targetId)
                    .orElseThrow { EntityNotFoundException("댓글을 찾을 수 없습니다.") }
            }
        }
        
        // 신고 생성
        val report = Report.create(
            reporter = reporter,
            reportType = request.reportType,
            targetType = request.targetType,
            targetId = request.targetId,
            content = request.content
        )
        
        val savedReport = reportRepository.save(report)
        return savedReport.id
    }

    /**
     * 신고 조회
     */
    @Transactional(readOnly = true)
     fun getReport(reportId: Long): ReportResponse {
        val report = reportRepository.findById(reportId)
            .orElseThrow { EntityNotFoundException("신고를 찾을 수 없습니다.") }
        
        return ReportResponse.from(report)
    }

    /**
     * 모든 신고 목록 조회
     */
    @Transactional(readOnly = true)
     fun getReports(pageable: Pageable): PageResponse<ReportResponse> {
        val reportsPage = reportRepository.findAll(pageable)
        
        return PageResponse.from(reportsPage.map { ReportResponse.from(it) })
    }

    /**
     * 상태별 신고 목록 조회
     */
    @Transactional(readOnly = true)
     fun getReportsByStatus(status: ReportStatus, pageable: Pageable): PageResponse<ReportResponse> {
        val reportsPage = reportRepository.findByStatus(status, pageable)
        
        return PageResponse.from(reportsPage.map { ReportResponse.from(it) })
    }

    /**
     * 대상별 신고 목록 조회
     */
    @Transactional(readOnly = true)
     fun getReportsByTarget(
        targetType: ReportTargetType,
        targetId: Long,
        pageable: Pageable
    ): PageResponse<ReportResponse> {
        val reportsPage = reportRepository.findByTargetTypeAndTargetId(targetType, targetId, pageable)
        
        return PageResponse.from(reportsPage.map { ReportResponse.from(it) })
    }

    /**
     * 신고자별 신고 목록 조회
     */
    @Transactional(readOnly = true)
     fun getReportsByReporter(reporterId: Long, pageable: Pageable): PageResponse<ReportResponse> {
        val reportsPage = reportRepository.findByReporterId(reporterId, pageable)
        
        return PageResponse.from(reportsPage.map { ReportResponse.from(it) })
    }

    /**
     * 신고 상태 업데이트
     */
     fun updateReportStatus(reportId: Long, request: ReportUpdateRequest, processor: Member): Long {
        val report = reportRepository.findById(reportId)
            .orElseThrow { EntityNotFoundException("신고를 찾을 수 없습니다.") }
        
        report.updateStatus(request.status, processor, request.comment)
        
        return report.id
    }

    /**
     * 이미 신고했는지 여부 확인
     */
    @Transactional(readOnly = true)
     fun hasReported(reporterId: Long, targetType: ReportTargetType, targetId: Long): Boolean {
        return reportRepository.existsByReporterIdAndTargetTypeAndTargetId(reporterId, targetType, targetId)
    }
} 