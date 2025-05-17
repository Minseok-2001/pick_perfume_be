package ym_cosmetic.pick_perfume_be.community.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class BoardCreateRequest(
    @field:NotBlank(message = "게시판 이름은 필수입니다")
    val name: String,
    
    @field:NotBlank(message = "게시판 표시 이름은 필수입니다")
    val displayName: String,
    
    val description: String? = null,
    
    @field:NotNull(message = "표시 순서는 필수입니다")
    val displayOrder: Int = 0
) 