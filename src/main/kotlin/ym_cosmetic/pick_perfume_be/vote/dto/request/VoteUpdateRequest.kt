package ym_cosmetic.pick_perfume_be.vote.dto.request

import jakarta.validation.constraints.NotBlank

data class VoteUpdateRequest(
    @field:NotBlank(message = "투표 값은 필수입니다")
    val value: String
)