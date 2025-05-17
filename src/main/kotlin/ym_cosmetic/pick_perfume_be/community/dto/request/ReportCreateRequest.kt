package ym_cosmetic.pick_perfume_be.community.dto.request

import jakarta.validation.constraints.NotNull
import ym_cosmetic.pick_perfume_be.community.enums.ReportTargetType
import ym_cosmetic.pick_perfume_be.community.enums.ReportType

data class ReportCreateRequest(
    @field:NotNull(message = "신고 유형은 필수입니다.")
    val reportType: ReportType,

    @field:NotNull(message = "신고 대상 유형은 필수입니다.")
    val targetType: ReportTargetType,

    @field:NotNull(message = "신고 대상 ID는 필수입니다.")
    val targetId: Long,

    val content: String?
) 