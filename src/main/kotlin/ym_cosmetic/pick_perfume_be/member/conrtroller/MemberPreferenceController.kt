package ym_cosmetic.pick_perfume_be.member.conrtroller

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ym_cosmetic.pick_perfume_be.common.dto.response.ApiResponse
import ym_cosmetic.pick_perfume_be.member.dto.MemberPreferenceDto
import ym_cosmetic.pick_perfume_be.recommendation.service.MemberPreferenceService

@RestController
@RequestMapping("/members")
class MemberPreferenceController(
    private val memberPreferenceService: MemberPreferenceService
) {
    @GetMapping("/me/preferences")
    fun getMyPreferences(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ApiResponse<MemberPreferenceDto> {
        val memberId = userDetails.username.toLong()
        val preferences = memberPreferenceService.getMemberPreferences(memberId)
        return ApiResponse.success(preferences)
    }

    @GetMapping("/{memberId}/preferences")
    fun getMemberPreferences(
        @PathVariable memberId: Long
    ): ApiResponse<MemberPreferenceDto> {
        val preferences = memberPreferenceService.getMemberPreferences(memberId)
        return ApiResponse.success(preferences)
    }
}