// designer/dto/response/DesignerSummaryResponse.kt
package ym_cosmetic.pick_perfume_be.designer.dto.response

import ym_cosmetic.pick_perfume_be.designer.entity.Designer

data class DesignerSummaryResponse(
    val id: Long,
    val name: String,
    val photoUrl: String?,
    val country: String?
) {
    companion object {
        fun from(designer: Designer): DesignerSummaryResponse {
            return DesignerSummaryResponse(
                id = designer.id!!,
                name = designer.name,
                photoUrl = designer.photo?.url,
                country = designer.country?.name,
            )
        }
    }
}