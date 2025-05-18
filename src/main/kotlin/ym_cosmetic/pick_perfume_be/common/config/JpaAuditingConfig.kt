import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import ym_cosmetic.pick_perfume_be.member.repository.MemberRepository
import java.util.*

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
class JpaAuditingConfig(
    private val memberRepository: MemberRepository
) {

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
                Optional.empty()
            }
        }
    }
}