package ym_cosmetic.pick_perfume_be.security.config

import jakarta.servlet.annotation.WebListener
import jakarta.servlet.http.HttpSession
import jakarta.servlet.http.HttpSessionEvent
import jakarta.servlet.http.HttpSessionListener
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer
import org.springframework.session.web.http.CookieSerializer
import org.springframework.session.web.http.DefaultCookieSerializer
import java.util.concurrent.ConcurrentHashMap

/**
 * 세션 설정 및 관리 클래스
 * - JDBC 기반 세션 저장소 설정
 * - 세션 쿠키 설정
 * - 세션 생성/소멸 이벤트 처리
 * - 동시 로그인 제어
 */
@Configuration
@EnableJdbcHttpSession(
    maxInactiveIntervalInSeconds = 3600,  // 세션 만료 시간: 1시간
    tableName = "SPRING_SESSION"  // 세션 테이블 이름
)
@WebListener
class SessionConfig : AbstractHttpSessionApplicationInitializer(), HttpSessionListener {

    @Value("\${app.session.cookie.secure:true}")
    private var secureCookie: Boolean = true

    @Value("\${app.session.cookie.same-site:Lax}")
    private lateinit var sameSite: String

    @Value("\${app.session.cookie.domain:}")
    private lateinit var domain: String

    /**
     * 쿠키 설정을 위한 직렬화 도구 빈 등록
     */
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

    companion object {
        private val sessions = ConcurrentHashMap<String, HttpSession>()

        @Synchronized
        fun findSessionByMemberId(memberId: Long): String? {
            for (key in sessions.keys) {
                val session = sessions[key]
                if (session != null) {
                    val sessionMemberId = session.getAttribute("MEMBER_ID") as? Long
                    if (sessionMemberId != null && sessionMemberId == memberId) {
                        return key
                    }
                }
            }
            return null
        }

        @Synchronized
        fun removeConcurrentSession(memberId: Long) {
            val sessionId = findSessionByMemberId(memberId)
            if (!sessionId.isNullOrEmpty()) {
                try {
                    val session = sessions[sessionId]
                    session?.setAttribute("expired", true)
                    sessions.remove(sessionId)
                } catch (e: Exception) {
                }
            }
        }


    }


    override fun sessionCreated(event: HttpSessionEvent) {
        val session = event.session
        sessions[session.id] = session
        println("세션 생성됨: ${session.id}, 총 세션 수: ${sessions.size}")
    }


    override fun sessionDestroyed(event: HttpSessionEvent) {
        val sessionId = event.session.id
        sessions.remove(sessionId)
        println("세션 소멸됨: $sessionId, 총 세션 수: ${sessions.size}")
    }
}