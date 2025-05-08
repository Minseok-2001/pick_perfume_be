package ym_cosmetic.pick_perfume_be.common.vo

import jakarta.persistence.Embeddable

@Embeddable
data class ImageUrl(val url: String) {
    fun getS3Key(): String? {
        return if (url.contains("amazonaws.com")) {
            url.substringAfterLast("/")
        } else null
    }

    fun getThumbnailUrl(): String {
        return if (url.contains("amazonaws.com")) {
            "${url.substringBeforeLast(".")}_thumbnail.${url.substringAfterLast(".")}"
        } else url
    }
}