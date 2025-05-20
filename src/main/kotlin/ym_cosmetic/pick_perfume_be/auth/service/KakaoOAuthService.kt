package ym_cosmetic.pick_perfume_be.auth.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import ym_cosmetic.pick_perfume_be.auth.dto.KakaoOAuthToken
import ym_cosmetic.pick_perfume_be.auth.dto.KakaoUserInfo

@Service
class KakaoOAuthService(private val restTemplate: RestTemplate) {

    @Value("\${oauth2.kakao.client-id}")
    private lateinit var clientId: String

    @Value("\${oauth2.kakao.redirect-uri}")
    private lateinit var redirectUri: String

    @Value("\${oauth2.kakao.client-secret}")
    private lateinit var clientSecret: String

    // 인증 코드로 액세스 토큰 요청
    fun getAccessToken(code: String): KakaoOAuthToken {
        val tokenUri = "https://kauth.kakao.com/oauth/token"
        
        val headers = HttpHeaders()
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
        
        val params = LinkedMultiValueMap<String, String>()
        params.add("grant_type", "authorization_code")
        params.add("client_id", clientId)
        params.add("redirect_uri", redirectUri)
        params.add("code", code)
        if (clientSecret.isNotBlank()) {
            params.add("client_secret", clientSecret)
        }
        
        val request = HttpEntity(params, headers)
        
        val response = restTemplate.postForEntity(tokenUri, request, KakaoOAuthToken::class.java)
        return response.body ?: throw RuntimeException("카카오 액세스 토큰 획득 실패")
    }
    
    // 액세스 토큰으로 사용자 정보 요청
    fun getUserInfo(accessToken: String): KakaoUserInfo {
        val userInfoUri = "https://kapi.kakao.com/v2/user/me"
        
        val headers = HttpHeaders()
        headers.setBearerAuth(accessToken)
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
        
        val request = HttpEntity<MultiValueMap<String, String>>(headers)
        
        val response = restTemplate.exchange(
            userInfoUri,
            HttpMethod.GET,
            request,
            KakaoUserInfo::class.java
        )
        
        return response.body ?: throw RuntimeException("카카오 사용자 정보 획득 실패")
    }
    
    // 카카오 로그인 URL 생성
    fun getKakaoLoginUrl(): String {
        return UriComponentsBuilder
            .fromHttpUrl("https://kauth.kakao.com/oauth/authorize")
            .queryParam("client_id", clientId)
            .queryParam("redirect_uri", redirectUri)
            .queryParam("response_type", "code")
            .toUriString()
    }
}