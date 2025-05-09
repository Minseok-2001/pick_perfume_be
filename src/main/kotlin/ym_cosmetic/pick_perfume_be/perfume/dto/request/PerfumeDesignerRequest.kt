package ym_cosmetic.pick_perfume_be.perfume.dto.request

import ym_cosmetic.pick_perfume_be.perfume.enums.DesignerRole

data class PerfumeDesignerRequest(
    val designerName: String,
    val role: DesignerRole,
    val description: String?
)