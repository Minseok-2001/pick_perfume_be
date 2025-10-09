package ym_cosmetic.pick_perfume_be.perfume.dto.response

import ym_cosmetic.pick_perfume_be.common.vo.ImageUrl
import ym_cosmetic.pick_perfume_be.perfume.entity.PerfumeAiImage
import ym_cosmetic.pick_perfume_be.perfume.enums.PerfumeAiImagePromptType

data class PerfumeAiImageResponse(
    val id: Long?,
    val promptType: PerfumeAiImagePromptType,
    val themeLabel: String,
    val visualFocus: String,
    val atmosphere: String,
    val prompt: String,
    val imageUrl: String,
    val voteCount: Long,
    val selectedByCurrentUser: Boolean,
    val isLegacyFallback: Boolean
) {
    companion object {
        fun from(
            aiImage: PerfumeAiImage,
            voteCount: Long = 0,
            selectedByCurrentUser: Boolean = false
        ): PerfumeAiImageResponse {
            return PerfumeAiImageResponse(
                id = aiImage.id,
                promptType = aiImage.promptType,
                themeLabel = aiImage.promptType.themeLabel,
                visualFocus = aiImage.promptType.visualFocus,
                atmosphere = aiImage.promptType.atmosphere,
                prompt = aiImage.prompt,
                imageUrl = aiImage.imageUrl.url,
                voteCount = voteCount,
                selectedByCurrentUser = selectedByCurrentUser,
                isLegacyFallback = false
            )
        }

        fun legacy(imageUrl: ImageUrl): PerfumeAiImageResponse {
            return PerfumeAiImageResponse(
                id = null,
                promptType = PerfumeAiImagePromptType.STORYBOARD_IMPRESSION,
                themeLabel = "legacy-single",
                visualFocus = PerfumeAiImagePromptType.STORYBOARD_IMPRESSION.visualFocus,
                atmosphere = PerfumeAiImagePromptType.STORYBOARD_IMPRESSION.atmosphere,
                prompt = "Legacy AI preview generated before prompt diversification (exact prompt unavailable).",
                imageUrl = imageUrl.url,
                voteCount = 0,
                selectedByCurrentUser = false,
                isLegacyFallback = true
            )
        }
    }
}
