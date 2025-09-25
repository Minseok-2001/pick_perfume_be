package ym_cosmetic.pick_perfume_be.perfume.service

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ym_cosmetic.pick_perfume_be.common.vo.ImageUrl
import ym_cosmetic.pick_perfume_be.infrastructure.gemini.GeminiImageService
import ym_cosmetic.pick_perfume_be.infrastructure.r2.R2Service
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume
import ym_cosmetic.pick_perfume_be.perfume.enums.PerfumeAiImageProcessStatus
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeAiImageRequestRepository
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeRepository
import ym_cosmetic.pick_perfume_be.perfume.vo.NoteType

@Service
class PerfumeAiImageGenerationWorker(
    private val perfumeRepository: PerfumeRepository,
    private val aiImageRequestRepository: PerfumeAiImageRequestRepository,
    private val r2Service: R2Service,
    private val geminiImageService: GeminiImageService
) {

    @Async
    @Transactional
    fun generateAsync(requestId: Long) {
        val request = aiImageRequestRepository.findById(requestId).orElse(null) ?: return

        if (request.status != PerfumeAiImageProcessStatus.QUEUED) {
            return
        }

        request.markProcessing()

        val perfumeId = request.perfume.id ?: run {
            request.markFailed("Perfume identifier is missing")
            logger.warn("Perfume id missing on request {}", requestId)
            return
        }
        val perfume = perfumeRepository.findByIdWithCreatorAndBrand(perfumeId)
            ?: run {
                request.markFailed("Perfume not found: $perfumeId")
                logger.warn("Perfume not found while generating AI preview (perfumeId={})", perfumeId)
                return
            }

        if (perfume.aiImage != null) {
            request.markSkipped("AI preview already exists")
            return
        }

        val generated = try {
            geminiImageService.generateImage(
                prompt = buildPerfumeImagePrompt(perfume),
                referenceImageUrl = perfume.image?.url
            )
        } catch (ex: Exception) {
            request.markFailed("Gemini API error: ${ex.message}")
            logger.error("Gemini API call failed for perfumeId={}", perfumeId, ex)
            return
        }

        if (generated == null) {
            request.markFailed("Gemini API returned no image")
            logger.warn("Gemini API did not return image for perfumeId={}", perfumeId)
            return
        }

        val fileExtension = determineFileExtension(generated.mimeType)
        val fileName = "ai-preview-$perfumeId-${System.currentTimeMillis()}$fileExtension"

        val imageUrl = try {
            r2Service.uploadFile(
                dirPath = "perfumes/$perfumeId/ai",
                fileName = fileName,
                bytes = generated.data,
                contentType = generated.mimeType
            )
        } catch (ex: Exception) {
            request.markFailed("Failed to upload image: ${ex.message}")
            logger.error("Failed to upload AI preview to R2 (perfumeId={})", perfumeId, ex)
            return
        }

        perfume.updateAiImage(ImageUrl(imageUrl))
        request.markSuccess()
    }

    private fun buildPerfumeImagePrompt(perfume: Perfume): String {
        val accords = perfume.getAccords()
            .sortedBy { it.position ?: Int.MAX_VALUE }
            .map { it.accord.name }
        val mainAccord = accords.firstOrNull()
        val supportingAccords = accords.drop(1)

        val topNotes = perfume.getNotesByType(NoteType.TOP).map { it.note.name }
        val middleNotes = perfume.getNotesByType(NoteType.MIDDLE).map { it.note.name }
        val baseNotes = perfume.getNotesByType(NoteType.BASE).map { it.note.name }

        return buildString {
            appendLine("Create a single, high-quality concept image that conveys the mood of this perfume. The image should help users imagine the scent profile. Avoid any text overlays.")
            appendLine("Perfume: ${perfume.name}")
            appendLine("Brand: ${perfume.brand.name}")
            mainAccord?.let { appendLine("Primary accord: $it") }
            if (supportingAccords.isNotEmpty()) {
                appendLine("Supporting accords: ${supportingAccords.joinToString()}")
            }
            if (topNotes.isNotEmpty()) {
                appendLine("Top notes: ${topNotes.joinToString()}")
            }
            if (middleNotes.isNotEmpty()) {
                appendLine("Heart notes: ${middleNotes.joinToString()}")
            }
            if (baseNotes.isNotEmpty()) {
                appendLine("Base notes: ${baseNotes.joinToString()}")
            }
            perfume.content?.takeIf { it.isNotBlank() }?.let {
                appendLine("Narrative inspiration: ${it.trim()}")
            }
            append("Style guidance: focus on atmosphere, lighting, and textures that fit the accords. Make it feel immersive without showing product packaging.")
        }.trim()
    }

    private fun determineFileExtension(mimeType: String): String {
        return when (mimeType.lowercase()) {
            "image/png" -> ".png"
            "image/webp" -> ".webp"
            "image/gif" -> ".gif"
            "image/bmp" -> ".bmp"
            "image/svg+xml" -> ".svg"
            "image/jpeg", "image/jpg" -> ".jpg"
            else -> ".jpg"
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PerfumeAiImageGenerationWorker::class.java)
    }
}
