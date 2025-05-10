package ym_cosmetic.pick_perfume_be.batch.scheduler

import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory
import java.util.*

@Component
@EnableScheduling
class BatchScheduler(
    private val jobLauncher: JobLauncher,
    
    @Qualifier("perfumeImportJob")
    private val perfumeImportJob: Job,
    
    @Qualifier("perfumeIndexJob")
    private val perfumeIndexJob: Job
) {
    private val logger = LoggerFactory.getLogger(BatchScheduler::class.java)

    // 매일 02:00에 향수 CSV 데이터 가져오기
    @Scheduled(cron = "0 0 2 * * ?")
    fun runPerfumeImportJob() {
        try {
            logger.info("향수 CSV 가져오기 작업 시작")
            val jobParameters = JobParametersBuilder()
                .addDate("startDate", Date())
                .addString("source", "scheduler")
                .toJobParameters()
            
            val jobExecution = jobLauncher.run(perfumeImportJob, jobParameters)
            
            logger.info("향수 CSV 가져오기 완료. 상태: {}", jobExecution.status)
        } catch (e: Exception) {
            logger.error("향수 CSV 가져오기 작업 실패", e)
        }
    }
    
    // 매시간 10분마다 ElasticSearch 인덱싱 작업 실행
    @Scheduled(cron = "0 10/60 * * * ?")
    fun runPerfumeIndexJob() {
        try {
            logger.info("향수 ElasticSearch 인덱싱 작업 시작")
            val jobParameters = JobParametersBuilder()
                .addDate("startDate", Date())
                .addString("source", "scheduler")
                .toJobParameters()
            
            val jobExecution = jobLauncher.run(perfumeIndexJob, jobParameters)
            
            logger.info("향수 ElasticSearch 인덱싱 완료. 상태: {}", jobExecution.status)
        } catch (e: Exception) {
            logger.error("향수 ElasticSearch 인덱싱 작업 실패", e)
        }
    }
    
    // 수동으로 작업 실행하기 위한 메서드
    fun runPerfumeImportJobManually(source: String): String {
        try {
            logger.info("향수 CSV 가져오기 작업 수동 실행 (소스: {})", source)
            val jobParameters = JobParametersBuilder()
                .addDate("startDate", Date())
                .addString("source", source)
                .toJobParameters()
            
            val jobExecution = jobLauncher.run(perfumeImportJob, jobParameters)
            
            return "향수 CSV 가져오기 작업 실행 완료. 상태: ${jobExecution.status}"
        } catch (e: Exception) {
            logger.error("향수 CSV 가져오기 작업 수동 실행 실패", e)
            return "향수 CSV 가져오기 작업 실패: ${e.message}"
        }
    }
    
    fun runPerfumeIndexJobManually(source: String): String {
        try {
            logger.info("향수 ElasticSearch 인덱싱 작업 수동 실행 (소스: {})", source)
            val jobParameters = JobParametersBuilder()
                .addDate("startDate", Date())
                .addString("source", source)
                .toJobParameters()
            
            val jobExecution = jobLauncher.run(perfumeIndexJob, jobParameters)
            
            return "향수 ElasticSearch 인덱싱 작업 실행 완료. 상태: ${jobExecution.status}"
        } catch (e: Exception) {
            logger.error("향수 ElasticSearch 인덱싱 작업 수동 실행 실패", e)
            return "향수 ElasticSearch 인덱싱 작업 실패: ${e.message}"
        }
    }
} 