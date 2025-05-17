package ym_cosmetic.pick_perfume_be.community.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*
import ym_cosmetic.pick_perfume_be.common.dto.response.ApiResponse
import ym_cosmetic.pick_perfume_be.community.dto.request.ReportCreateRequest
import ym_cosmetic.pick_perfume_be.community.dto.request.ReportUpdateRequest
import ym_cosmetic.pick_perfume_be.community.dto.response.PageResponse
import ym_cosmetic.pick_perfume_be.community.dto.response.ReportResponse
import ym_cosmetic.pick_perfume_be.community.enums.ReportStatus
import ym_cosmetic.pick_perfume_be.community.enums.ReportTargetType
import ym_cosmetic.pick_perfume_be.community.service.ReportService
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.member.enums.MemberRole
import ym_cosmetic.pick_perfume_be.security.CurrentMember
import ym_cosmetic.pick_perfume_be.security.RequireRole

@RestController
@RequestMapping("/api/reports")
@Tag(name = "신고 API", description = "게시글/댓글 신고 관련 API")
class ReportController(
    private val reportService: ReportService
) {
    @PostMapping
    @Operation(summary = "신고 등록", description = "게시글이나 댓글을 신고합니다.")
    fun createReport(
        @Valid @RequestBody request: ReportCreateRequest,
        @CurrentMember member: Member
    ): ApiResponse<Long> {
            val reportId = reportService.createReport(request, member)
            return ApiResponse.success("신고가 등록되었습니다.", reportId)
    }

    @GetMapping("/{reportId}")
    @RequireRole(MemberRole.ADMIN)
    @Operation(summary = "신고 조회", description = "특정 신고를 조회합니다. (관리자 전용)")
    fun getReport(
        @PathVariable reportId: Long,
        @CurrentMember member: Member
    ): ApiResponse<ReportResponse> {
            val report = reportService.getReport(reportId)
            return ApiResponse.success("신고 조회 성공", report)
    }

    @GetMapping
    @RequireRole(MemberRole.ADMIN)
    @Operation(summary = "신고 목록 조회", description = "신고 목록을 조회합니다. (관리자 전용)")
    fun getReports(
        @PageableDefault(size = 20) pageable: Pageable,
        @CurrentMember member: Member
    ): ApiResponse<PageResponse<ReportResponse>> {
        val reports = reportService.getReports(pageable)
        return ApiResponse.success("신고 목록 조회 성공", reports)

    }

    @GetMapping("/status/{status}")
    @RequireRole(MemberRole.ADMIN)
    @Operation(summary = "상태별 신고 목록 조회", description = "특정 상태의 신고 목록을 조회합니다. (관리자 전용)")
    fun getReportsByStatus(
        @PathVariable status: ReportStatus,
        @PageableDefault(size = 20) pageable: Pageable,
        @CurrentMember member: Member
    ): ApiResponse<PageResponse<ReportResponse>> {
        val reports = reportService.getReportsByStatus(status, pageable)
        return ApiResponse.success("상태별 신고 목록 조회 성공", reports)

    }

    @GetMapping("/target")
    @RequireRole(MemberRole.ADMIN)
    @Operation(summary = "대상별 신고 목록 조회", description = "특정 대상(게시글/댓글)의 신고 목록을 조회합니다. (관리자 전용)")
    fun getReportsByTarget(
        @RequestParam targetType: ReportTargetType,
        @RequestParam targetId: Long,
        @PageableDefault(size = 20) pageable: Pageable,
        @CurrentMember member: Member
    ): ApiResponse<PageResponse<ReportResponse>> {
        val reports = reportService.getReportsByTarget(targetType, targetId, pageable)
        return ApiResponse.success("대상별 신고 목록 조회 성공", reports)

    }

    @PutMapping("/{reportId}")
    @RequireRole(MemberRole.ADMIN)
    @Operation(summary = "신고 상태 업데이트", description = "신고 상태를 업데이트합니다. (관리자 전용)")
    fun updateReportStatus(
        @PathVariable reportId: Long,
        @Valid @RequestBody request: ReportUpdateRequest,
        @CurrentMember member: Member
    ): ApiResponse<Long> {
        val updatedReportId = reportService.updateReportStatus(reportId, request, member)
        return ApiResponse.success("신고 상태가 업데이트되었습니다.", updatedReportId)

    }

    @GetMapping("/check")
    @Operation(summary = "신고 여부 확인", description = "특정 대상에 대한 신고 여부를 확인합니다.")
    fun checkReported(
        @RequestParam targetType: ReportTargetType,
        @RequestParam targetId: Long,
        @CurrentMember member: Member
    ): ApiResponse<Boolean> {
        val hasReported = reportService.hasReported(member.id!!, targetType, targetId)
        return ApiResponse.success("신고 여부 확인 성공", hasReported)

    }
}