package ym_cosmetic.pick_perfume_be.infrastructure.batch.config

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableBatchProcessing
@EnableScheduling
class BatchConfig {

    @Bean
    fun taskExecutor(): TaskExecutor {
        val executor = SimpleAsyncTaskExecutor("batch-task-executor")
        executor.concurrencyLimit = 10
        return executor
    }
} 