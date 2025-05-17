package ym_cosmetic.pick_perfume_be.community.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class PostCreateRequest(
    @field:NotBlank(message = "제목은 필수입니다.")
    @field:Size(min = 2, max = 100, message = "제목은 2자 이상 100자 이하여야 합니다.")
    val title: String,
    
    @field:NotBlank(message = "내용은 필수입니다.")
    val content: String,
    
    @field:NotNull(message = "게시판은 필수입니다.")
    val boardId: Long,
    
    val perfumeIds: List<Long>? = null
) 