package ym_cosmetic.pick_perfume_be.community.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ym_cosmetic.pick_perfume_be.common.dto.response.ApiResponse
import ym_cosmetic.pick_perfume_be.community.dto.response.PresignedUrlResponse
import ym_cosmetic.pick_perfume_be.community.service.CommunityFileService
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.security.CurrentMember

@RestController
@RequestMapping("/api/communities")
class CommunityFileController {

    @Autowired
    private lateinit var communityFileService: CommunityFileService



    @GetMapping("/presigned-url")
    fun getPresignedUrl(
        @CurrentMember member: Member,
        @RequestParam("fileKey") fileKey: String,
    ): ApiResponse<PresignedUrlResponse> {
        val presignedUrl = communityFileService.getPresignedUrl(member.id!!, fileKey)
        return ApiResponse.success("임시 URL이 생성되었습니다", presignedUrl)
    }
} 