package ym_cosmetic.pick_perfume_be.security.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.session.events.SessionCreatedEvent
import org.springframework.session.events.SessionDeletedEvent
import org.springframework.session.events.SessionExpiredEvent

/**
 * 세션 이벤트를 처리하는 리스너 클래스
 * 세션 생성, 만료, 삭제 등의 이벤트를 감지하고 처리합니다.
 */
@Configuration
class SecuritySessionEventListener {
    
    private val logger = LoggerFactory.getLogger(SecuritySessionEventListener::class.java)
    
    @EventListener
    fun handleSessionCreatedEvent(event: SessionCreatedEvent) {
        val sessionId = event.sessionId
        logger.debug("세션 생성됨: {}", maskSessionId(sessionId))
        // 세션 생성 시 추가 작업을 수행할 수 있습니다.
        // 예: 세션 생성 통계 수집, 사용자 활동 로깅 등
    }
    
    @EventListener
    fun handleSessionExpiredEvent(event: SessionExpiredEvent) {
        val sessionId = event.sessionId
        logger.debug("세션 만료됨: {}", maskSessionId(sessionId))
        // 세션 만료 시 필요한 정리 작업을 수행할 수 있습니다.
        // 예: 리소스 정리, 사용자 상태 업데이트 등
    }
    
    @EventListener
    fun handleSessionDeletedEvent(event: SessionDeletedEvent) {
        val sessionId = event.sessionId
        logger.debug("세션 삭제됨: {}", maskSessionId(sessionId))
        // 세션 삭제 시 필요한 작업을 수행할 수 있습니다.
        // 예: 로그아웃 처리, 세션 정보 정리 등
    }
    
    /**
     * 세션 ID를 마스킹하여 로그에 안전하게 출력합니다.
     */
    private fun maskSessionId(sessionId: String): String {
        if (sessionId.length <= 8) return "***"
        return sessionId.substring(0, 4) + "..." + sessionId.substring(sessionId.length - 4)
    }
} 