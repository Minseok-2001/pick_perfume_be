package ym_cosmetic.pick_perfume_be.infrastructure.gemini

import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.Base64

@Service
class GeminiImageService(
    private val restTemplate: RestTemplate,
    @Value("\${gemini.api-key:}") private val apiKey: String,
    @Value("\${gemini.base-url:https://generativelanguage.googleapis.com}") private val baseUrl: String,
    @Value("\${gemini.model:gemini-2.5-flash-image-preview}") private val model: String,
    @Value("\${gemini.enabled:true}") private val enabled: Boolean
) {

    fun generateImage(prompt: String, referenceImageUrl: String? = null): GeminiGeneratedImage? {
        if (!enabled) {
            return null
        }

        if (apiKey.isBlank()) {
            logger.warn("Gemini API key is not configured; skipping image generation.")
            return null
        }

        val parts = mutableListOf(
            GeminiPartRequest(text = prompt)
        )

        referenceImageUrl
            ?.let { fetchInlineData(it) }
            ?.let { parts.add(GeminiPartRequest(inlineData = it)) }

        val requestBody = GeminiGenerateContentRequest(
            contents = listOf(GeminiContentRequest(parts = parts))
        )

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            set("x-goog-api-key", apiKey)
        }

        return runCatching {
            val response = restTemplate.postForEntity(
                "${'$'}baseUrl/v1beta/models/${'$'}model:generateContent",
                HttpEntity(requestBody, headers),
                GeminiGenerateContentResponse::class.java
            )

            response.body?.candidates
                ?.firstOrNull()
                ?.content?.parts
                ?.firstOrNull { it.inlineData != null }
                ?.inlineData
                ?.let { inline ->
                    val data = inline.data ?: return@runCatching null
                    GeminiGeneratedImage(
                        mimeType = inline.mimeType ?: DEFAULT_MIME_TYPE,
                        data = data
                    )
                }
        }.onFailure { ex ->
            logger.error("Failed to generate image via Gemini API", ex)
        }.getOrNull()
    }

    private fun fetchInlineData(url: String): GeminiInlineDataRequest? {
        return runCatching {
            val bytes = restTemplate.getForObject(url, ByteArray::class.java)
            if (bytes != null) {
                GeminiInlineDataRequest(
                    mimeType = inferMimeType(url),
                    data = Base64.getEncoder().encodeToString(bytes)
                )
            } else {
                null
            }
        }.onFailure { ex ->
            logger.warn("Failed to fetch reference image from {}", url, ex)
        }.getOrNull()
    }

    private fun inferMimeType(url: String): String {
        val sanitized = url.substringBefore('?')
        val extension = sanitized.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "bmp" -> "image/bmp"
            "svg" -> "image/svg+xml"
            else -> DEFAULT_MIME_TYPE
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GeminiImageService::class.java)
        private const val DEFAULT_MIME_TYPE = "image/jpeg"
    }
}

data class GeminiGeneratedImage(
    val mimeType: String,
    val data: String
)

private data class GeminiGenerateContentRequest(
    val contents: List<GeminiContentRequest>
)

private data class GeminiContentRequest(
    val parts: List<GeminiPartRequest>
)

private data class GeminiPartRequest(
    val text: String? = null,
    @JsonProperty("inline_data")
    val inlineData: GeminiInlineDataRequest? = null
)

private data class GeminiInlineDataRequest(
    @JsonProperty("mime_type")
    val mimeType: String,
    val data: String
)

private data class GeminiGenerateContentResponse(
    val candidates: List<GeminiCandidate>?
)

private data class GeminiCandidate(
    val content: GeminiContent?
)

private data class GeminiContent(
    val parts: List<GeminiPartResponse>?
)

private data class GeminiPartResponse(
    val text: String? = null,
    @JsonProperty("inline_data")
    val inlineData: GeminiInlineDataResponse? = null
)

private data class GeminiInlineDataResponse(
    @JsonProperty("mime_type")
    val mimeType: String?,
    val data: String?
)
