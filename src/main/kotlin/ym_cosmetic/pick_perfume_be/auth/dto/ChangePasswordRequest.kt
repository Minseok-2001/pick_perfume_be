package ym_cosmetic.pick_perfume_be.auth.dto

import jakarta.validation.constraints.NotBlank

data class ChangePasswordRequest(
    @field:NotBlank(message = "현재 비밀번호는 필수입니다.")
    val currentPassword: String,
    
    @field:NotBlank(message = "새 비밀번호는 필수입니다.")
    val newPassword: String,
    
    @field:NotBlank(message = "새 비밀번호 확인은 필수입니다.")
    val confirmPassword: String
)



