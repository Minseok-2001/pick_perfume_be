package ym_cosmetic.pick_perfume_be.perfume.dto.response

import ym_cosmetic.pick_perfume_be.common.vo.ImageUrl

data class PerfumeAiImageResponse(
    val url: String
) {
    companion object {
        fun from(imageUrl: ImageUrl): PerfumeAiImageResponse {
            return PerfumeAiImageResponse(url = imageUrl.url)
        }
    }
}
