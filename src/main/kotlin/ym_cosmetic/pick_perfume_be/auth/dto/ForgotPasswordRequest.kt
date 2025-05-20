package ym_cosmetic.pick_perfume_be.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class ForgotPasswordRequest(
    @field:NotBlank(message = "이메일은 필수입니다.")
    @field:Email(message = "유효한 이메일 형식이 아닙니다.")
    val email: String
)