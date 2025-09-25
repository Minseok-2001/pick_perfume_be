package ym_cosmetic.pick_perfume_be.infrastructure.s3

import org.springframework.web.multipart.MultipartFile

interface S3Service {
    fun uploadFile(dirPath: String, file: MultipartFile): String
    fun uploadFile(dirPath: String, fileName: String, bytes: ByteArray, contentType: String): String
    fun deleteFile(url: String)
}
