package ym_cosmetic.pick_perfume_be.auth.controller

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import ym_cosmetic.pick_perfume_be.auth.dto.ChangePasswordRequest
import ym_cosmetic.pick_perfume_be.auth.dto.ForgotPasswordRequest
import ym_cosmetic.pick_perfume_be.auth.dto.LoginRequest
import ym_cosmetic.pick_perfume_be.auth.dto.LoginResponse
import ym_cosmetic.pick_perfume_be.auth.dto.ResetPasswordRequest
import ym_cosmetic.pick_perfume_be.auth.service.AuthService
import ym_cosmetic.pick_perfume_be.common.dto.response.ApiResponse

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {


    @PostMapping("/login")
    fun login(
        @RequestBody request: @Valid LoginRequest
    ): ApiResponse<LoginResponse?> {
        val response: LoginResponse? = authService.login(request)
        return ApiResponse.success(response)
    }

    @PutMapping("/password")
    fun changePassword(
        @RequestBody request: @Valid ChangePasswordRequest
    ): ApiResponse<String> {
        authService.changePassword(request)
        return ApiResponse.success("비밀번호가 성공적으로 변경되었습니다.")
    }

    @PostMapping("/password/forgot")
    fun forgotPassword(
        @RequestBody request: @Valid ForgotPasswordRequest
    ): ApiResponse<String> {
        authService.sendPasswordResetEmail(request.email)
        return ApiResponse.success("비밀번호 재설정 이메일이 발송되었습니다.")
    }

    @PostMapping("/password/reset")
    fun resetPassword(
        @RequestBody request: @Valid ResetPasswordRequest
    ): ApiResponse<String> {
        authService.resetPassword(request)
        return ApiResponse.success("비밀번호가 성공적으로 재설정되었습니다.")
    }


    @PostMapping("/logout")
    fun logout() {
        authService.logout()
    }
}