package ym_cosmetic.pick_perfume_be.auth.controller

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ym_cosmetic.pick_perfume_be.auth.dto.LoginRequest
import ym_cosmetic.pick_perfume_be.auth.dto.LoginResponse
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

    @PostMapping("/logout")
    fun logout() {
        authService.logout()
    }
}