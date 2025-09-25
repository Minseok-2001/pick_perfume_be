package ym_cosmetic.pick_perfume_be.infrastructure.s3

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class S3ServiceImpl : S3Service {
    override fun uploadFile(dirPath: String, file: MultipartFile): String {
        val fileName = file.originalFilename ?: "file-${System.currentTimeMillis()}"
        val contentType = file.contentType ?: "application/octet-stream"
        return uploadFile(dirPath, fileName, file.bytes, contentType)
    }

    override fun uploadFile(dirPath: String, fileName: String, bytes: ByteArray, contentType: String): String {
        // Implement the logic to upload the file to S3 and return the URL
        return "https://example.com/$dirPath/$fileName"
    }

    override fun deleteFile(url: String) {
        // Implement the logic to delete the file from S3
    }
}
