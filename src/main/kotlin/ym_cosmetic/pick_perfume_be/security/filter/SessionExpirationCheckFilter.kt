package ym_cosmetic.pick_perfume_be.security.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import ym_cosmetic.pick_perfume_be.security.interceptor.AuthenticationInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * 세션 만료 확인 필터
 * 세션의 마지막 접근 시간을 확인하여 일정 시간 이상 활동이 없는 세션을 만료시킵니다.
 */
@Component
class SessionExpirationCheckFilter(
    private val authInterceptor: AuthenticationInterceptor
) : OncePerRequestFilter() {

    @Value("\${app.session.idle-timeout-minutes:30}")
    private var idleTimeoutMinutes: Long = 30
    
    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest, 
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val session = request.getSession(false)
        
        if (session != null) {
            val lastAccessTime = session.getAttribute(AuthenticationInterceptor.LAST_ACCESS_TIME_KEY) as? Long
            
            if (lastAccessTime != null) {
                val currentTime = System.currentTimeMillis()
                val idleTime = currentTime - lastAccessTime
                val idleTimeoutMillis = TimeUnit.MINUTES.toMillis(idleTimeoutMinutes)
                
                // 세션 타임아웃 확인
                if (idleTime > idleTimeoutMillis) {
                    // 세션 만료 처리
                    authInterceptor.clearMemberFromSession(request)
                    
                    // API 요청인 경우 401 응답
                    if (isApiRequest(request)) {
                        response.status = HttpServletResponse.SC_UNAUTHORIZED
                        response.contentType = "application/json"
                        response.writer.write("{\"error\":\"세션이 만료되었습니다. 다시 로그인해주세요.\"}")
                        return
                    }
                    // 웹 요청인 경우 로그인 페이지로 리다이렉트
                    else {
                        response.sendRedirect("/login?expired=true")
                        return
                    }
                }
            }
        }
        
        filterChain.doFilter(request, response)
    }
    
    /**
     * API 요청인지 확인하는 메서드
     */
    private fun isApiRequest(request: HttpServletRequest): Boolean {
        val requestURI = request.requestURI
        return requestURI.startsWith("/api/") || 
               request.getHeader("Accept")?.contains("application/json") == true ||
               request.getHeader("Content-Type")?.contains("application/json") == true
    }
} 