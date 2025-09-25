package ym_cosmetic.pick_perfume_be.perfume.service

import io.github.resilience4j.ratelimiter.RateLimiter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.perfume.entity.PerfumeAiImageRequest
import ym_cosmetic.pick_perfume_be.perfume.enums.PerfumeAiImageProcessStatus
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeAiImageRequestRepository
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeRepository

@Service
class PerfumeAiImageGenerationScheduler(
    private val perfumeRepository: PerfumeRepository,
    private val aiImageRequestRepository: PerfumeAiImageRequestRepository,
    private val rateLimiter: RateLimiter,
    private val worker: PerfumeAiImageGenerationWorker
) {

    @Transactional
    fun schedule(perfumeId: Long, member: Member?, ipAddress: String?) {
        val perfume = perfumeRepository.findById(perfumeId).orElse(null) ?: return
        if (perfume.aiImage != null) {
            return
        }

        val latestRequest = aiImageRequestRepository.findTopByPerfumeIdOrderByCreatedAtDesc(perfumeId)
        if (latestRequest != null && latestRequest.status in ACTIVE_STATUSES) {
            return
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
                message = if (permissionGranted) null else "Rate limit exceeded"
            )
        )

        if (!permissionGranted) {
            logger.warn("AI preview generation rate limited for perfumeId={} ip={}", perfumeId, ipAddress)
            return
        }

        worker.generateAsync(request.id!!)
    }

    companion object {
        private val ACTIVE_STATUSES = setOf(
            PerfumeAiImageProcessStatus.QUEUED,
            PerfumeAiImageProcessStatus.PROCESSING
        )
        private val logger = LoggerFactory.getLogger(PerfumeAiImageGenerationScheduler::class.java)
    }
}

