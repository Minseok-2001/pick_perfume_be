package ym_cosmetic.pick_perfume_be.common.config

import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import io.github.resilience4j.ratelimiter.RateLimiterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class Resilience4jConfig {

    @Bean
    fun perfumeAiImageRateLimiter(): RateLimiter {
        val config = RateLimiterConfig.custom()
            .limitRefreshPeriod(Duration.ofSeconds(30))
            .limitForPeriod(20)
            .timeoutDuration(Duration.ZERO)
            .build()
        val registry = RateLimiterRegistry.of(config)
        return registry.rateLimiter("perfume-ai-image")
    }
}
