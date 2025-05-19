package ym_cosmetic.pick_perfume_be.infrastructure.r2

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
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
class R2Service {
    
    @Autowired
    private lateinit var r2Config: R2Config
    
    @Autowired
    private lateinit var s3Client: S3Client
    
    @Autowired
    private lateinit var s3Presigner: S3Presigner


     fun createPresignedUrl(dirPath: String, fileName: String): PresignedUrlResponse {
        try {
            val keyName = "$dirPath/$fileName"
            
            val putRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .putObjectRequest(
                    PutObjectRequest.builder()
                        .bucket(r2Config.getBucketName())
                        .key(keyName)
                        .build()
                )
                .build()
            
            val presignedPutRequest = s3Presigner.presignPutObject(putRequest)
            
            val publicUrl = "${r2Config.getPublicUrl()}/$keyName"
            return PresignedUrlResponse(
                presignedUrl = presignedPutRequest.url().toString(),
                publicUrl = publicUrl
            )
        } catch (e: Exception) {
            throw R2Exception("Presigned URL 생성 중 오류 발생: ${e.message}")
        }
    }
    
    // 다운로드용 Presigned URL 생성 메서드 (필요시 사용)
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
            throw R2Exception("다운로드 URL 생성 중 오류 발생: ${e.message}")
        }
    }
}