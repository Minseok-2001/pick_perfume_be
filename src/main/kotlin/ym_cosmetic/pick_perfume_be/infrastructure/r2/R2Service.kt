package ym_cosmetic.pick_perfume_be.infrastructure.r2

import org.springframework.stereotype.Service
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import ym_cosmetic.pick_perfume_be.common.exception.R2Exception
import ym_cosmetic.pick_perfume_be.community.dto.response.PresignedUrlResponse
import java.time.Duration

@Service
class R2Service(
    private val r2Config: R2Config,
    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner
) {

    fun uploadFile(dirPath: String, fileName: String, bytes: ByteArray, contentType: String): String {
        val key = buildKey(dirPath, fileName)

        try {
            val putRequest = PutObjectRequest.builder()
                .bucket(r2Config.getBucketName())
                .key(key)
                .contentType(contentType)
                .build()

            s3Client.putObject(putRequest, RequestBody.fromBytes(bytes))
            return buildPublicUrl(key)
        } catch (e: Exception) {
            throw R2Exception("R2 객체 업로드에 실패했습니다: ${e.message}", e)
        }
    }

    fun createPresignedUrl(dirPath: String, fileName: String): PresignedUrlResponse {
        val key = buildKey(dirPath, fileName)

        try {
            val putRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .putObjectRequest(
                    PutObjectRequest.builder()
                        .bucket(r2Config.getBucketName())
                        .key(key)
                        .build()
                )
                .build()

            val presignedPutRequest = s3Presigner.presignPutObject(putRequest)

            return PresignedUrlResponse(
                presignedUrl = presignedPutRequest.url().toString(),
                publicUrl = buildPublicUrl(key)
            )
        } catch (e: Exception) {
            throw R2Exception("R2 업로드용 프리사인 URL 생성에 실패했습니다: ${e.message}", e)
        }
    }

    fun createPresignedDownloadUrl(key: String, expirationInMinutes: Long): String {
        try {
            val getRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expirationInMinutes))
                .getObjectRequest(
                    GetObjectRequest.builder()
                        .bucket(r2Config.getBucketName())
                        .key(key)
                        .build()
                )
                .build()

            val presignedGetRequest = s3Presigner.presignGetObject(getRequest)

            return presignedGetRequest.url().toString()
        } catch (e: Exception) {
            throw R2Exception("R2 다운로드용 프리사인 URL 생성에 실패했습니다: ${e.message}", e)
        }
    }

    private fun buildKey(dirPath: String, fileName: String): String {
        val normalizedDir = dirPath.trim().trim('/').trim()
        val normalizedFile = fileName.trim().trim('/').trim()

        if (normalizedFile.isEmpty()) {
            throw R2Exception("업로드할 파일 이름이 비어 있습니다.")
        }

        return if (normalizedDir.isBlank()) {
            normalizedFile
        } else {
            "$normalizedDir/$normalizedFile"
        }
    }

    private fun buildPublicUrl(key: String): String {
        return "${r2Config.getPublicUrl().trimEnd('/')}/$key"
    }
}
