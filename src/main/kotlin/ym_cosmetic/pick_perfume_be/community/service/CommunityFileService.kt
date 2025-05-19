package ym_cosmetic.pick_perfume_be.community.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ym_cosmetic.pick_perfume_be.community.dto.response.PresignedUrlResponse
import ym_cosmetic.pick_perfume_be.infrastructure.r2.R2Service

@Service
class CommunityFileService {

    @Autowired
    private lateinit var r2Service: R2Service


    fun getPresignedDownloadUrl(memberId: Long, fileKey: String, expirationInMinutes: Long = 60): String {
        val keyPath = "community/users/$memberId/$fileKey"
        return r2Service.createPresignedDownloadUrl(keyPath, expirationInMinutes)
    }

    fun getPresignedUrl(memberId: Long, fileName: String): PresignedUrlResponse {
        val dirPath = "community/users/$memberId"
          return r2Service.createPresignedUrl(dirPath, fileName)
    }
    
    private fun isAllowedFileType(file: MultipartFile): Boolean {
        val allowedExtensions = listOf("jpg", "jpeg", "png", "gif", "webp", "pdf")
        val originalFilename = file.originalFilename ?: return false
        val fileExtension = originalFilename.substringAfterLast('.', "").lowercase()
        
        return allowedExtensions.contains(fileExtension)
    }
} 