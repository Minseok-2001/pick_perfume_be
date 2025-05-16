package ym_cosmetic.pick_perfume_be.common.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.OncePerRequestFilter
import ym_cosmetic.pick_perfume_be.security.filter.CsrfFilter
import ym_cosmetic.pick_perfume_be.security.filter.SecurityHeadersFilter
import ym_cosmetic.pick_perfume_be.security.filter.XssFilter
import java.util.*

@Configuration
class SecurityConfig {
    @Value("\${SWAGGER_USERNAME:admin}")
    private lateinit var swaggerUsername: String

    @Value("\${SWAGGER_PASSWORD:password}")
    private lateinit var swaggerPassword: String

    @Bean
    fun xssFilterRegistration(xssFilter: XssFilter): FilterRegistrationBean<XssFilter?> {
        val registration: FilterRegistrationBean<XssFilter?> =
            FilterRegistrationBean<XssFilter?>(xssFilter)
        registration.order = 1
        registration.addUrlPatterns("/*")
        return registration
    }

    @Bean
    fun csrfFilterRegistration(csrfFilter: CsrfFilter): FilterRegistrationBean<CsrfFilter?> {
        val registration: FilterRegistrationBean<CsrfFilter?> =
            FilterRegistrationBean<CsrfFilter?>(csrfFilter)
        registration.order = 2
        registration.addUrlPatterns("/*")
        return registration
    }

    @Bean
    fun securityHeadersFilterRegistration(
        filter: SecurityHeadersFilter
    ): FilterRegistrationBean<SecurityHeadersFilter?> {
        val registration: FilterRegistrationBean<SecurityHeadersFilter?> =
            FilterRegistrationBean<SecurityHeadersFilter?>(
                filter
            )
        registration.order = 3
        registration.addUrlPatterns("/*")
        return registration
    }

    @Bean
    fun swaggerSecurityFilterRegistration(): FilterRegistrationBean<SwaggerSecurityFilter> {
        val registration = FilterRegistrationBean<SwaggerSecurityFilter>()
        registration.filter = SwaggerSecurityFilter(swaggerUsername, swaggerPassword)
        registration.order = 4
        registration.addUrlPatterns("/docs/*", "/swagger-ui/*", "/api-docs/*")
        return registration
    }

    inner class SwaggerSecurityFilter(
        private val username: String,
        private val password: String
    ) : OncePerRequestFilter() {
        override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain
        ) {
            val auth = request.getHeader("Authorization")

            if (auth != null && auth.startsWith("Basic ")) {
                val base64Credentials = auth.substring("Basic ".length).trim()
                val credentials = String(Base64.getDecoder().decode(base64Credentials))
                val values = credentials.split(":", limit = 2)

                if (values.size == 2 && values[0] == username && values[1] == password) {
                    filterChain.doFilter(request, response)
                    return
                }
            }

            response.setHeader("WWW-Authenticate", "Basic realm=\"Swagger UI\"")
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
        }
    }
}