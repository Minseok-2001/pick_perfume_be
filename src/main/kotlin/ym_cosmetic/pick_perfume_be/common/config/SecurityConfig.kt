package ym_cosmetic.pick_perfume_be.common.config

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ym_cosmetic.pick_perfume_be.security.filter.CsrfFilter
import ym_cosmetic.pick_perfume_be.security.filter.SecurityHeadersFilter
import ym_cosmetic.pick_perfume_be.security.filter.XssFilter

@Configuration
class SecurityConfig {
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
}