package ym_cosmetic.pick_perfume_be.perfume.service

import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ym_cosmetic.pick_perfume_be.common.vo.ImageUrl
import ym_cosmetic.pick_perfume_be.infrastructure.gemini.GeminiImageService
import ym_cosmetic.pick_perfume_be.infrastructure.r2.R2Service
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume
import ym_cosmetic.pick_perfume_be.perfume.entity.PerfumeAiImage
import ym_cosmetic.pick_perfume_be.perfume.enums.PerfumeAiImageProcessStatus
import ym_cosmetic.pick_perfume_be.perfume.enums.PerfumeAiImagePromptType
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeAiImageRequestRepository
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeAiImageRepository
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeRepository
import ym_cosmetic.pick_perfume_be.perfume.vo.NoteType

@Service
class PerfumeAiImageGenerationWorker(
    private val perfumeRepository: PerfumeRepository,
    private val aiImageRequestRepository: PerfumeAiImageRequestRepository,
    private val perfumeAiImageRepository: PerfumeAiImageRepository,
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

        val promptType = request.promptType
        val alreadyGenerated = perfumeAiImageRepository.findByPerfumeIdAndPromptType(perfumeId, promptType)
        if (alreadyGenerated != null) {
            request.markSkipped("AI preview already exists for $promptType")
            return
        }

        val prompt = buildPerfumeImagePrompt(perfume, promptType)
        request.prompt = prompt

        val generated = try {
            geminiImageService.generateImage(
                prompt = prompt,
                referenceImageUrl = perfume.image?.url
            )
        } catch (ex: Exception) {
            request.markFailed("Gemini API error: ${ex.message}")
            logger.error("Gemini API call failed for perfumeId={} promptType={}", perfumeId, promptType, ex)
            return
        }

        if (generated == null) {
            request.markFailed("Gemini API returned no image")
            logger.warn("Gemini API did not return image for perfumeId={} promptType={}", perfumeId, promptType)
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
            logger.error("Failed to upload AI preview to R2 (perfumeId={}, promptType={})", perfumeId, promptType, ex)
            return
        }

        val aiImage = PerfumeAiImage.create(
            perfume = perfume,
            promptType = promptType,
            prompt = prompt,
            imageUrl = ImageUrl(imageUrl)
        )

        try {
            val savedImage = perfumeAiImageRepository.save(aiImage)
            perfume.upsertAiImage(savedImage)
            if (perfume.aiImage == null) {
                perfume.updateAiImage(ImageUrl(imageUrl))
            }
        } catch (ex: DataIntegrityViolationException) {
            request.markSkipped("AI preview already stored for $promptType")
            logger.info(
                "AI preview already persisted for perfumeId={} promptType={}, skipping storage",
                perfumeId,
                promptType
            )
            return
        }

        request.markSuccess("Generated $promptType image")
        logger.info("AI preview generated for perfumeId={} promptType={}", perfumeId, promptType)
    }

    private fun buildPerfumeImagePrompt(
        perfume: Perfume,
        promptType: PerfumeAiImagePromptType
    ): String {
        val accords = perfume.getAccords()
            .sortedBy { it.position ?: Int.MAX_VALUE }
            .map { it.accord.name }
        val mainAccord = accords.firstOrNull()
        val supportingAccords = accords.drop(1)

        val topNotes = perfume.getNotesByType(NoteType.TOP).map { it.note.name }
        val middleNotes = perfume.getNotesByType(NoteType.MIDDLE).map { it.note.name }
        val baseNotes = perfume.getNotesByType(NoteType.BASE).map { it.note.name }

        return buildString {
            appendLine("Create a high-fidelity ${promptType.themeLabel} visual that helps users imagine the fragrance without showing any packaging or typography.")
            appendLine("Ensure the scene feels premium and transportive, avoiding literal perfume bottles unless using abstract silhouettes.")
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
            when (promptType) {
                PerfumeAiImagePromptType.STORYBOARD_IMPRESSION -> {
                    appendLine("Scene direction: dynamic, cinematic vignettes that imply motion and emotion, capturing fleeting human presence through silhouettes or gestures.")
                    appendLine("Camera guidance: anamorphic lens feel, shallow depth of field, gentle lens flares, and layered foreground/background depth.")
                    append("Mood: ${promptType.atmosphere}; emphasize ${promptType.visualFocus}.")
                }
                PerfumeAiImagePromptType.EDITORIAL_STILL_LIFE -> {
                    appendLine("Scene direction: art-directed still life with tactile props (fabric, glass, botanicals) that echo the accords without literal branding.")
                    appendLine("Lighting: crisp studio lighting with controlled shadows, high contrast accents, and reflections on luxurious surfaces.")
                    append("Mood: ${promptType.atmosphere}; emphasize ${promptType.visualFocus}.")
                }
                PerfumeAiImagePromptType.CHROMATIC_AURA -> {
                    appendLine("Scene direction: abstract sensory landscape with layered gradients, ethereal particles, and flowing light trails inspired by the scent pyramid.")
                    appendLine("Composition: immersive color fields with subtle textures, refracted light, and soft bokeh to suggest diffusion.")
                    append("Mood: ${promptType.atmosphere}; emphasize ${promptType.visualFocus}.")
                }
            }
            appendLine()
            append("Rendering rules: no text, no logos, no humans in focus, no perfume bottles unless abstracted; deliver as a single cohesive 16:9 illustration.")
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
