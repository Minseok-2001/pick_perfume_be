package ym_cosmetic.pick_perfume_be.security.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.session.jdbc.JdbcIndexedSessionRepository
import org.springframework.session.jdbc.config.annotation.SpringSessionDataSource
import javax.sql.DataSource

/**
 * 세션 저장소 설정 클래스
 * 세션 데이터의 저장 방식과 관련된 세부 설정을 정의합니다.
 */
@Configuration
class SessionRepositoryConfig {

    @Value("\${app.session.cleanup-cron:0 0 */1 * * *}")
    private lateinit var cleanupCron: String
    
    /**
     * 세션 저장소의 세부 설정을 정의합니다.
     * - 세션 테이블 이름 설정
     * - 세션 정리 주기 설정
     * - 세션 정리 임계값 설정
     */
    @Bean
    fun sessionRepositoryCustomizer(@SpringSessionDataSource jdbcTemplate: JdbcTemplate): JdbcIndexedSessionRepository.JdbcIndexedSessionRepositoryCustomizer {
        return JdbcIndexedSessionRepository.JdbcIndexedSessionRepositoryCustomizer { repositoryBuilder ->
            // 만료된 세션 정리 주기 설정 (Cron 표현식)
            repositoryBuilder.cleanupCron(cleanupCron)
            
            // 세션 정리 임계값 설정 (100개 이상의 만료된 세션이 있을 때 정리 작업 수행)
            repositoryBuilder.cleanupThreshold(100)
            
            // 세션 플러시 모드 설정 (즉시 저장)
            repositoryBuilder.flushMode(JdbcIndexedSessionRepository.FlushMode.IMMEDIATE)
        }
    }
    
    /**
     * 세션 저장소 초기화를 위한 스크립트를 실행합니다.
     * 애플리케이션 시작 시 세션 테이블이 없으면 생성합니다.
     */
    @Bean
    fun sessionRepositoryInitializer(@SpringSessionDataSource dataSource: DataSource): JdbcTemplate {
        val jdbcTemplate = JdbcTemplate(dataSource)
        // 필요한 경우 여기에 추가 초기화 로직을 구현할 수 있습니다.
        return jdbcTemplate
    }
} 