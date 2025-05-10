package ym_cosmetic.pick_perfume_be.batch.config

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.core.task.TaskExecutor
import javax.sql.DataSource
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager

@Configuration
@EnableBatchProcessing
class BatchConfig(private val dataSource: DataSource) {

    @Bean
    fun taskExecutor(): TaskExecutor {
        val executor = SimpleAsyncTaskExecutor("batch-executor-")
        executor.concurrencyLimit = 10
        return executor
    }

    @Bean
    fun transactionManager(): PlatformTransactionManager {
        return DataSourceTransactionManager(dataSource)
    }

    @Bean
    fun jobRepository(): JobRepository {
        val factory = JobRepositoryFactoryBean()
        factory.setDataSource(dataSource)
        factory.setTransactionManager(transactionManager())
        factory.afterPropertiesSet()
        return factory.getObject()
    }
} 