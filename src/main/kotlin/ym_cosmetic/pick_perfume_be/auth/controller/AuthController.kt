package ym_cosmetic.pick_perfume_be.auth.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ym_cosmetic.pick_perfume_be.auth.service.AuthService

@RestController
@RequestMapping("/auth")
class AuthController {

    private val authService: AuthService? = null


    @PostMapping("/login")
    fun login() {
        // 로그인 처리 로직
    }

    @PostMapping("/logout")
    fun logout() {
        // 로그아웃 처리 로직
    }
}