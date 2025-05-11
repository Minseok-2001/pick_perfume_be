package ym_cosmetic.pick_perfume_be.batch.job

import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.data.RepositoryItemReader
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.data.domain.Sort
import org.springframework.transaction.PlatformTransactionManager
import ym_cosmetic.pick_perfume_be.batch.processor.PerfumeIndexProcessor
import ym_cosmetic.pick_perfume_be.batch.writer.PerfumeIndexWriter
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeRepository
import ym_cosmetic.pick_perfume_be.search.document.PerfumeDocument

@Configuration
class PerfumeIndexJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val taskExecutor: TaskExecutor,
    private val perfumeRepository: PerfumeRepository,
    private val perfumeIndexProcessor: PerfumeIndexProcessor,
    private val perfumeIndexWriter: PerfumeIndexWriter
) {
    private val logger = LoggerFactory.getLogger(PerfumeIndexJobConfig::class.java)
    private val CHUNK_SIZE = 20

    @Bean
    fun perfumeIndexJob(): Job {
        return JobBuilder("perfumeIndexJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(databaseToElasticSearchStep())
            .build()
    }

    @Bean
    fun databaseToElasticSearchStep(): Step {
        return StepBuilder("databaseToElasticSearchStep", jobRepository)
            .chunk<Perfume, PerfumeDocument>(CHUNK_SIZE, transactionManager)
            .reader(perfumeRepositoryReader())
            .processor(perfumeIndexProcessor)
            .writer(perfumeIndexWriter)
            .taskExecutor(taskExecutor)
            .build()
    }

    @Bean
    fun perfumeRepositoryReader(): RepositoryItemReader<Perfume> {
        val sorts = LinkedHashMap<String, Sort.Direction>()
        sorts["id"] = Sort.Direction.ASC

        return RepositoryItemReaderBuilder<Perfume>()
            .name("perfumeRepositoryReader")
            .repository(perfumeRepository)
            .methodName("findByIsApprovedAndSearchSyncedOrderById")
            .arguments(listOf(true, false))
            .pageSize(CHUNK_SIZE)
            .sorts(sorts)
            .build()
    }
} 