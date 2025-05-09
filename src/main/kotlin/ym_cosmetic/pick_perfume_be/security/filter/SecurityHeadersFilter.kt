package ym_cosmetic.pick_perfume_be.security.filter

import jakarta.servlet.*
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class SecurityHeadersFilter : Filter {
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(
        request: ServletRequest?,
        response: ServletResponse?,
        chain: FilterChain
    ) {
        val httpResponse = response as HttpServletResponse

        // XSS 방지
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block")

        // 클릭재킹 방지
        httpResponse.setHeader("X-Frame-Options", "DENY")

        // MIME 타입 스니핑 방지
        httpResponse.setHeader("X-Content-Type-Options", "nosniff")

        // HTTPS 강제
        httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains")

        chain.doFilter(request, response)
    }
}