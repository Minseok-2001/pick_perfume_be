package ym_cosmetic.pick_perfume_be.auth.service

import org.springframework.stereotype.Service

@Service
class AuthService {

    companion object {
        const val USER_SESSION_KEY = "USER_ID"
    }

    fun login(userId: Long) {
        // 로그인 처리 로직
        // 세션에 사용자 ID 저장
        // session.setAttribute(USER_SESSION_KEY, userId)
    }

    fun logout() {
        // 로그아웃 처리 로직
        // 세션에서 사용자 ID 제거
        // session.removeAttribute(USER_SESSION_KEY)
    }

}