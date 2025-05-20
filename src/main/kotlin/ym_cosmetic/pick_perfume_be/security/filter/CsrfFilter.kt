package ym_cosmetic.pick_perfume_be.security.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import ym_cosmetic.pick_perfume_be.common.exception.ForbiddenException
import java.io.IOException
import java.util.*

@Component
class CsrfFilter : OncePerRequestFilter() {
    @Value("\${app.session.cookie.domain:}")
    private lateinit var domain: String

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest, response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val session = request.getSession(false)
        val method = request.method
        val host = request.serverName
        val origin = request.getHeader("Origin")

        // 디버깅 로그 추가
        println("Request Origin: $origin")
        println("Configured Domain: $domain")
        println("Request URI: ${request.requestURI}")
        println("Request Method: $method")

        // Origin 검증 로직 수정
        val isAllowedOrigin = if (domain.isNotBlank() && origin != null) {
            origin.contains(domain)
        } else {
            false
        }

        // 추가 디버깅
        println("Is Allowed Origin: $isAllowedOrigin")

        if ("localhost" == host || "127.0.0.1" == host ||
            request.requestURI.startsWith("/docs") ||
            request.requestURI.startsWith("/api-docs") ||
            request.requestURI.startsWith("/swagger-ui") ||
            request.requestURI.contains("/users/signup") ||
            request.requestURI.contains("/auth/login") ||
            request.requestURI.contains("/auth/password/forgot") ||
            // 여기서 수정된 로직 사용
            isAllowedOrigin
        ) {
            println("CSRF 검증 건너뜁니다.")
            filterChain.doFilter(request, response)
            return
        }

        if ("GET" == method || "HEAD" == method || "OPTIONS" == method) {
            if (session != null && session.getAttribute(CSRF_TOKEN) == null) {
                val token: String? = UUID.randomUUID().toString()
                session.setAttribute(CSRF_TOKEN, token)
                response.setHeader(CSRF_HEADER, token)

                // 디버깅: 토큰 정보 출력
                println("CSRF Token 생성: $token")
            }
            filterChain.doFilter(request, response)
            return
        }

        if (session != null) {
            val sessionToken = session.getAttribute(CSRF_TOKEN) as String?
            val requestToken = request.getHeader(CSRF_HEADER)

            // 디버깅: 토큰 비교 정보 출력
            println("세션 토큰: $sessionToken")
            println("요청 토큰: $requestToken")

            if (sessionToken == null || sessionToken != requestToken) {
                println("CSRF 토큰 불일치 - 403 에러")
                throw ForbiddenException("CSRF 토큰이 유효하지 않습니다.")
            }
        } else {
            println("세션이 존재하지 않음 - 403 에러")
            throw ForbiddenException("세션이 존재하지 않습니다.")
        }

        filterChain.doFilter(request, response)
    }

    companion object {
        private const val CSRF_TOKEN = "CSRF_TOKEN"
        private const val CSRF_HEADER = "X-CSRF-TOKEN"
    }
}