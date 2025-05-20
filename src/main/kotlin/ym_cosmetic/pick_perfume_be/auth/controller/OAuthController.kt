package ym_cosmetic.pick_perfume_be.auth.controller

import org.springframework.web.bind.annotation.*
import ym_cosmetic.pick_perfume_be.auth.dto.LoginResponse
import ym_cosmetic.pick_perfume_be.auth.service.KakaoOAuthService
import ym_cosmetic.pick_perfume_be.auth.service.OAuthService
import ym_cosmetic.pick_perfume_be.common.dto.response.ApiResponse

@RestController
@RequestMapping("/api/oauth")
class OAuthController(
    private val kakaoOAuthService: KakaoOAuthService,
    private val oAuthService: OAuthService
) {

    @GetMapping("/kakao/login")
    fun getKakaoLoginUrl(): ApiResponse<String> {
        val loginUrl = kakaoOAuthService.getKakaoLoginUrl()
        return ApiResponse.success(loginUrl)
    }
    
    @GetMapping("/kakao/callback")
    fun kakaoCallback(@RequestParam code: String): ApiResponse<LoginResponse> {
        val token = kakaoOAuthService.getAccessToken(code)
        val userInfo = kakaoOAuthService.getUserInfo(token.accessToken)
        val loginResponse = oAuthService.loginKakaoUser(userInfo)
        return ApiResponse.success(loginResponse)
    }
}