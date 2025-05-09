package ym_cosmetic.pick_perfume_be.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import ym_cosmetic.pick_perfume_be.common.vo.ImageUrl

@Schema(description = "회원가입 요청")
data class SignupRequest(
    @Schema(description = "사용자 이름", example = "user123")
    @NotBlank(message = "사용자 이름은 필수입니다.")
    @Size(
        min = 3,
        max = 20,
        message = "사용자 이름은 3자에서 20자 사이여야 합니다."
    )
    val nickname: String,

    @Size(
        min = 2,
        max = 20,
        message = "이름은 2자에서 20자 사이여야 합니다."
    )
    @Schema(description = "이름", example = "홍길동")
    val name: String,

    @Schema(description = "비밀번호", example = "password123")
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(
        min = 6,
        max = 40,
        message = "비밀번호는 6자에서 40자 사이여야 합니다."
    )
    val password: String? = null,

    @Schema(description = "이메일", example = "user@example.com")
    @NotBlank(message = "이메일은 필수입니다.")
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
        message = "유효한 이메일 형식이어야 합니다."
    )
    val email: String,


    val profileImage: ImageUrl? = null,

    @Schema(description = "전화번호", example = "010-1234-5678")
    @Pattern(
        regexp = "^\\d{3}-\\d{3,4}-\\d{4}$",
        message = "전화번호는 010-1234-5678 형식이어야 합니다."
    )
    val phoneNumber: String? = null,

    )
