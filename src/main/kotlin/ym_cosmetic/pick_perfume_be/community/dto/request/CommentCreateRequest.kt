package ym_cosmetic.pick_perfume_be.community.dto.request

import jakarta.validation.constraints.NotBlank

data class CommentCreateRequest(
    @field:NotBlank(message = "내용은 필수입니다.")
    val content: String,
    
    val parentId: Long? = null
) 