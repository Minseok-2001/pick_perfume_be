package ym_cosmetic.pick_perfume_be.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.RequestAttributes
import java.util.Optional

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
class JpaAuditingConfig {
    
    @Bean
    fun auditorProvider(): AuditorAware<String> {
        return AuditorAware {
            if (RequestContextHolder.getRequestAttributes() != null) {
                val memberId = RequestContextHolder.currentRequestAttributes()
                    .getAttribute("MEMBER_ID", RequestAttributes.SCOPE_SESSION) as? Long
                
                if (memberId != null) {
                    Optional.of(memberId.toString())
                } else {
                    Optional.empty()
                }
            } else {
                // 배치 작업 등 RequestContext가 없는 경우 기본값 반환
                Optional.of("SYSTEM")
            }
        }
    }
}
