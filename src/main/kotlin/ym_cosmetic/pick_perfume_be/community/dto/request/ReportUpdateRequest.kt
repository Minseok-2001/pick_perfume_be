package ym_cosmetic.pick_perfume_be.community.dto.request

import jakarta.validation.constraints.NotNull
import ym_cosmetic.pick_perfume_be.community.enums.ReportStatus

data class ReportUpdateRequest(
    @field:NotNull(message = "신고 상태는 필수입니다.")
    val status: ReportStatus,
    
    val comment: String?
) 