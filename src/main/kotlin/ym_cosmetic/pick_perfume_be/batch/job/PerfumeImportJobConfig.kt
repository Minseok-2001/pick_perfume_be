package ym_cosmetic.pick_perfume_be.batch.job

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.transaction.PlatformTransactionManager
import ym_cosmetic.pick_perfume_be.batch.dto.PerfumeImportDto
import ym_cosmetic.pick_perfume_be.batch.processor.PerfumeProcessor
import ym_cosmetic.pick_perfume_be.batch.reader.PerfumeCsvReader
import ym_cosmetic.pick_perfume_be.batch.writer.PerfumeWriter
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume
import org.slf4j.LoggerFactory

@Configuration
class PerfumeImportJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val taskExecutor: TaskExecutor,
    private val perfumeCsvReader: PerfumeCsvReader,
    private val perfumeProcessor: PerfumeProcessor,
    private val perfumeWriter: PerfumeWriter
) {
    private val logger = LoggerFactory.getLogger(PerfumeImportJobConfig::class.java)
    private val CHUNK_SIZE = 50

    @Bean
    fun perfumeImportJob(): Job {
        return JobBuilder("perfumeImportJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(csvToDatabaseStep())
            .build()
    }

    @Bean
    fun csvToDatabaseStep(): Step {
        return StepBuilder("csvToDatabaseStep", jobRepository)
            .chunk<PerfumeImportDto, Perfume>(CHUNK_SIZE, transactionManager)
            .reader(perfumeCsvReader.createReader("classpath:data/perfumes.csv"))
            .processor(perfumeProcessor)
            .writer(perfumeWriter)
            .taskExecutor(taskExecutor)
            .build()
    }
} 