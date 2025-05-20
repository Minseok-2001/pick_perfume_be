package ym_cosmetic.pick_perfume_be.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import ym_cosmetic.pick_perfume_be.common.vo.ImageUrl

@Schema(description = "회원 정보 수정 요청")
data class UpdateMemberRequest (
    @Schema(description = "사용자 이름", example = "user123")
    val nickname: String? = null,

    @Schema(description = "이름", example = "홍길동")
    val name: String? = null,

    @Schema(description = "전화번호", example = "010-1234-5678")
    @Pattern(
        regexp = "^\\d{3}-\\d{3,4}-\\d{4}$",
        message = "전화번호는 010-1234-5678 형식이어야 합니다."
    )
    val phoneNumber: String? = null,

    @Schema(description = "썸네일", example = "https://example.com/image.jpg")
    val profileImage: ImageUrl? = null
)