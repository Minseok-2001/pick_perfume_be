package ym_cosmetic.pick_perfume_be.common.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextClosedEvent

@Configuration
class CoroutineScopeConfig {
    @Bean
    fun applicationCoroutineScope(
        @Autowired applicationContext: ConfigurableApplicationContext
    ): CoroutineScope {
        val job = SupervisorJob()

        applicationContext.addApplicationListener { event ->
            if (event is ContextClosedEvent) {
                job.cancel()
            }
        }

        return CoroutineScope(Dispatchers.IO + job)
    }
}
