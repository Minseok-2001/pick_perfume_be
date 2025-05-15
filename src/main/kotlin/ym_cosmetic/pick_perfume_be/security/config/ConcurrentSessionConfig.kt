package ym_cosmetic.pick_perfume_be.security.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.session.FindByIndexNameSessionRepository
import org.springframework.session.Session
import org.springframework.session.events.SessionCreatedEvent
import ym_cosmetic.pick_perfume_be.security.interceptor.AuthenticationInterceptor
import java.util.*

/**
 * 동시 세션 제어 설정 클래스
 * 사용자당 최대 세션 수를 제한하고 이전 세션을 관리합니다.
 */
@Configuration
class ConcurrentSessionConfig(
    private val sessionRepository: FindByIndexNameSessionRepository<out Session>,
    private val authenticationInterceptor: AuthenticationInterceptor
) {

    @Value("\${app.session.max-sessions-per-user:1}")
    private var maxSessionsPerUser: Int = 1
    
    @Value("\${app.session.expire-oldest-session:true}")
    private var expireOldestSession: Boolean = true
    
    /**
     * 세션 생성 이벤트를 감지하여 동시 세션 제어를 수행합니다.
     * 사용자당 최대 세션 수를 초과하는 경우 가장 오래된 세션을 만료시킵니다.
     */
    @EventListener
    fun handleSessionCreated(event: SessionCreatedEvent) {
        val session = sessionRepository.findById(event.sessionId) ?: return
        
        // 세션에서 사용자 ID 가져오기
        val memberId = session.getAttribute<Long>(AuthenticationInterceptor.MEMBER_ID_SESSION_KEY) ?: return
        
        // 사용자의 모든 세션 찾기
        val sessions = sessionRepository.findByIndexNameAndIndexValue(
            FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, 
            memberId.toString()
        )
        
        // 최대 세션 수를 초과하는 경우 처리
        if (sessions.size > maxSessionsPerUser && expireOldestSession) {
            // 세션을 생성 시간 기준으로 정렬
            val sortedSessions = sessions.values.sortedBy { it.creationTime }
            
            // 현재 세션을 제외한 가장 오래된 세션 만료 처리
            for (i in 0 until sortedSessions.size - maxSessionsPerUser) {
                val oldestSession = sortedSessions[i]
                if (oldestSession.id != event.sessionId) {
                    oldestSession.setAttribute("expired", true)
                    sessionRepository.save(oldestSession)
                }
            }
        }
    }
    
    /**
     * 세션에 사용자 식별자 인덱스를 설정합니다.
     * 이 메소드는 사용자 로그인 시 호출되어야 합니다.
     */
    fun setSessionPrincipalIndex(sessionId: String, memberId: Long) {
        val session = sessionRepository.findById(sessionId) ?: return
        session.setAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, memberId.toString())
        sessionRepository.save(session)
    }
} 