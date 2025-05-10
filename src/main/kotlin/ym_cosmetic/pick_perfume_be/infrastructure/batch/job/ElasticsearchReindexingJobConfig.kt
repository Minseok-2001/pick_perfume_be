package ym_cosmetic.pick_perfume_be.infrastructure.batch.job

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.data.RepositoryItemReader
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.data.domain.Sort
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.IndexQuery
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder
import org.springframework.transaction.PlatformTransactionManager
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeRepository
import ym_cosmetic.pick_perfume_be.search.document.PerfumeDocument
import ym_cosmetic.pick_perfume_be.search.mapper.PerfumeDocumentMapper

@Configuration
class ElasticsearchReindexingJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val taskExecutor: TaskExecutor,
    private val perfumeRepository: PerfumeRepository,
    private val elasticsearchOperations: ElasticsearchOperations,
    private val perfumeDocumentMapper: PerfumeDocumentMapper
) {

    @Bean
    fun elasticsearchReindexingJob(): Job {
        return JobBuilder("elasticsearchReindexingJob", jobRepository)
            .start(perfumeReindexingStep())
            .build()
    }

    @Bean
    fun perfumeReindexingStep(): Step {
        return StepBuilder("perfumeReindexingStep", jobRepository)
            .chunk<Perfume, PerfumeDocument>(100, transactionManager)
            .reader(perfumeReindexingReader())
            .processor(perfumeToDocumentProcessor())
            .writer(elasticsearchDocumentWriter())
            .taskExecutor(taskExecutor)
            .build()
    }

    @Bean
    fun perfumeReindexingReader(): ItemReader<Perfume> {
        return RepositoryItemReaderBuilder<Perfume>()
            .name("perfumeReindexingReader")
            .repository(perfumeRepository)
            .methodName("findBySearchSyncedFalse")
            .pageSize(100)
            .sorts(mapOf("id" to Sort.Direction.ASC))
            .build()
    }

    @Bean
    fun perfumeToDocumentProcessor(): ItemProcessor<Perfume, PerfumeDocument> {
        return ItemProcessor { perfume ->
            val document = perfumeDocumentMapper.toDocument(perfume)
            perfume.setSearchSynced(true) // 인덱싱 완료 표시
            document
        }
    }

    @Bean
    fun elasticsearchDocumentWriter(): ItemWriter<PerfumeDocument> {
        return ItemWriter { items ->
            val queries = items.map { document ->
                IndexQueryBuilder()
                    .withId(document.id.toString())
                    .withObject(document)
                    .build()
            }
            elasticsearchOperations.bulkIndex(
                queries,
                IndexCoordinates.of("perfumes")
            )
        }
    }
} 