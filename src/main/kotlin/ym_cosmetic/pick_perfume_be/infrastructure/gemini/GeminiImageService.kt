package ym_cosmetic.pick_perfume_be.infrastructure.gemini

import com.google.genai.Client
import com.google.genai.types.Content
import com.google.genai.types.GenerateContentConfig
import com.google.genai.types.Part
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.util.Locale

@Service
class GeminiImageService(
    private val restTemplate: RestTemplate,
    @Value("\${gemini.api-key:}") private val apiKey: String,
    @Value("\${gemini.model:models/gemini-2.5-flash-image-preview}") private val model: String,
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

        val parts = mutableListOf<Part>()

        referenceImageUrl
            ?.let { fetchReferenceImage(it) }
            ?.let { parts.add(Part.fromBytes(it.data, it.mimeType)) }

        parts.add(Part.fromText(prompt))

        return runCatching {
            Client.builder().apiKey(apiKey).build().use { client ->
                val response = client.models.generateContent(
                    model,
                    Content.fromParts(*parts.toTypedArray()),
                    GenerateContentConfig.builder()
                        .responseModalities(listOf("IMAGE"))
                        .build()
                )

                val blob = response.candidates()
                    .orElse(emptyList())
                    .firstOrNull()
                    ?.content()?.orElse(null)
                    ?.parts()?.orElse(emptyList())
                    ?.firstOrNull { it.inlineData().isPresent }
                    ?.inlineData()?.orElse(null)
                    ?: return@use null

                val data = blob.data().orElse(null) ?: return@use null

                GeminiGeneratedImage(
                    mimeType = blob.mimeType().orElse(DEFAULT_MIME_TYPE),
                    data = data
                )
            }
        }.onFailure { ex ->
            logger.error("Failed to generate image via Gemini SDK", ex)
        }.getOrNull()
    }

    private fun fetchReferenceImage(url: String): ReferenceImage? {
        return runCatching {
            val headers = HttpHeaders().apply {
                add(HttpHeaders.USER_AGENT, "pick-perfume-ai-image-client")
            }
            val request = RequestEntity<Any>(headers, HttpMethod.GET, URI.create(url))
            val response: ResponseEntity<ByteArray> = restTemplate.exchange(request, ByteArray::class.java)
            val body = response.body ?: return@runCatching null
            val contentType = response.headers.contentType?.toString() ?: inferMimeType(url)
            ReferenceImage(data = body, mimeType = contentType)
        }.onFailure { ex ->
            logger.warn("Failed to fetch reference image from {}", url, ex)
        }.getOrNull()
    }

    private fun inferMimeType(url: String): String {
        val sanitized = url.substringBefore('?')
        val extension = sanitized.substringAfterLast('.', "").lowercase(Locale.ROOT)
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
    val data: ByteArray
)

private data class ReferenceImage(
    val data: ByteArray,
    val mimeType: String
)
