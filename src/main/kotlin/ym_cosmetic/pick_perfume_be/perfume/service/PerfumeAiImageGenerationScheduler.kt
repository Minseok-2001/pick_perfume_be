package ym_cosmetic.pick_perfume_be.perfume.service

import io.github.resilience4j.ratelimiter.RateLimiter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.perfume.entity.PerfumeAiImageRequest
import ym_cosmetic.pick_perfume_be.perfume.enums.PerfumeAiImageProcessStatus
import ym_cosmetic.pick_perfume_be.perfume.enums.PerfumeAiImagePromptType
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeAiImageRequestRepository
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeAiImageRepository
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeRepository

@Service
class PerfumeAiImageGenerationScheduler(
    private val perfumeRepository: PerfumeRepository,
    private val aiImageRequestRepository: PerfumeAiImageRequestRepository,
    private val perfumeAiImageRepository: PerfumeAiImageRepository,
    private val rateLimiter: RateLimiter,
    private val worker: PerfumeAiImageGenerationWorker
) {

    @Transactional
    fun schedule(perfumeId: Long, member: Member?, ipAddress: String?) {
        val perfume = perfumeRepository.findById(perfumeId).orElse(null) ?: return
        val existingVariants = perfumeAiImageRepository.findByPerfumeId(perfumeId)
            .map { it.promptType }
            .toSet()
        val missingPromptTypes = PerfumeAiImagePromptType.entries.filterNot { it in existingVariants }

        if (missingPromptTypes.isEmpty()) {
            return
        }

        for (promptType in missingPromptTypes) {
            val latestRequest =
                aiImageRequestRepository.findTopByPerfumeIdAndPromptTypeOrderByCreatedAtDesc(perfumeId, promptType)
            if (latestRequest != null && latestRequest.status in ACTIVE_STATUSES) {
                continue
            }

            val permissionGranted = rateLimiter.acquirePermission()
            val status = if (permissionGranted) {
                PerfumeAiImageProcessStatus.QUEUED
            } else {
                PerfumeAiImageProcessStatus.RATE_LIMITED
            }

            val request = aiImageRequestRepository.save(
                PerfumeAiImageRequest.create(
                    perfume = perfume,
                    member = member,
                    ipAddress = ipAddress,
                    status = status,
                    promptType = promptType,
                    message = if (permissionGranted) null else "Rate limit exceeded"
                )
            )

            if (!permissionGranted) {
                logger.warn(
                    "AI preview generation rate limited for perfumeId={} promptType={} ip={}",
                    perfumeId,
                    promptType,
                    ipAddress
                )
                break
            }

            request.id?.let { dispatchToWorker(it) }
        }
    }

    private fun dispatchToWorker(requestId: Long) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
                override fun afterCommit() {
                    worker.generateAsync(requestId)
                }
            })
        } else {
            worker.generateAsync(requestId)
        }
    }

    companion object {
        private val ACTIVE_STATUSES = setOf(
            PerfumeAiImageProcessStatus.QUEUED,
            PerfumeAiImageProcessStatus.PROCESSING
        )
        private val logger = LoggerFactory.getLogger(PerfumeAiImageGenerationScheduler::class.java)
    }
}



