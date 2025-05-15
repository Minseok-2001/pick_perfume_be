package ym_cosmetic.pick_perfume_be.security.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer
import org.springframework.session.web.http.CookieSerializer
import org.springframework.session.web.http.DefaultCookieSerializer

@Configuration
@EnableJdbcHttpSession(
    maxInactiveIntervalInSeconds = 3600,  // 세션 만료 시간: 1시간
    tableName = "SPRING_SESSION"  // 세션 테이블 이름
)
class SessionConfig : AbstractHttpSessionApplicationInitializer() {
    
    @Value("\${app.session.cookie.secure:true}")
    private var secureCookie: Boolean = true
    
    @Value("\${app.session.cookie.same-site:Lax}")
    private lateinit var sameSite: String
    
    @Value("\${app.session.cookie.domain:}")
    private lateinit var domain: String
    
    @Bean
    fun cookieSerializer(): CookieSerializer {
        val serializer = DefaultCookieSerializer()
        
        serializer.setUseSecureCookie(secureCookie)
        serializer.setSameSite(sameSite)
        
        serializer.setCookieName("SCENTIST_SESSION")
        serializer.setCookiePath("/")
        
        if (domain.isNotBlank()) {
            serializer.setDomainName(domain)
        }
        
        return serializer
    }
}