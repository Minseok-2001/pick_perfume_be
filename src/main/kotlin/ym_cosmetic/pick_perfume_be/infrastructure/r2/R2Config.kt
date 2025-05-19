package ym_cosmetic.pick_perfume_be.infrastructure.r2

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.net.URI

@Configuration
class R2Config {
    @Value("\${cloudflare.r2.account.id}")
    private lateinit var accountId: String

    @Value("\${cloudflare.r2.access.key}")
    private lateinit var accessKeyId: String

    @Value("\${cloudflare.r2.access.secret}")
    private lateinit var secretAccessKey: String

    @Value("\${cloudflare.r2.bucket.name}")
    private lateinit var bucketName: String

    @Value("\${cloudflare.r2.public.url}")
    private lateinit var publicUrl: String

    @Bean
    fun s3Client(): S3Client {
        val credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey)
        val endpoint = URI.create("https://$accountId.r2.cloudflarestorage.com")
        
        return S3Client.builder()
            .region(Region.US_EAST_1)
            .endpointOverride(endpoint)
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build()
    }
    
    @Bean
    fun s3Presigner(): S3Presigner {
        val credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey)
        val endpoint = URI.create("https://$accountId.r2.cloudflarestorage.com")
        
        return S3Presigner.builder()
            .region(Region.US_EAST_1)
            .endpointOverride(endpoint)
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build()
    }

    fun getBucketName(): String = bucketName
    fun getPublicUrl(): String = publicUrl
}