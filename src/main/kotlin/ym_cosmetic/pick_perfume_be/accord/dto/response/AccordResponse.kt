// accord/dto/response/AccordResponse.kt
package ym_cosmetic.pick_perfume_be.accord.dto.response

import ym_cosmetic.pick_perfume_be.accord.entity.Accord

data class AccordResponse(
    val id: Long,
    val name: String,
    val color: String?
) {
    companion object {
        fun from(accord: Accord): AccordResponse {
            return AccordResponse(
                id = accord.id!!,
                name = accord.name,
                color = accord.color
            )
        }
    }
}