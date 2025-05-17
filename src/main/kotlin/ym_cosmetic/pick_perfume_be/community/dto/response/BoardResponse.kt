package ym_cosmetic.pick_perfume_be.community.dto.response

import ym_cosmetic.pick_perfume_be.community.entity.Board

data class BoardResponse(
    val id: Long,
    val name: String,
    val displayName: String,
    val description: String?,
    val isActive: Boolean,
    val displayOrder: Int
) {
    companion object {
        fun from(board: Board): BoardResponse {
            return BoardResponse(
                id = board.id,
                name = board.getName(),
                displayName = board.getDisplayName(),
                description = board.getDescription(),
                isActive = board.isActive(),
                displayOrder = board.getDisplayOrder()
            )
        }
    }
} 