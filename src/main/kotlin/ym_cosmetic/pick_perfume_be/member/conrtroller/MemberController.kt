package ym_cosmetic.pick_perfume_be.member.conrtroller

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import ym_cosmetic.pick_perfume_be.common.dto.response.ApiResponse
import ym_cosmetic.pick_perfume_be.community.dto.response.PresignedUrlResponse
import ym_cosmetic.pick_perfume_be.member.MemberService
import ym_cosmetic.pick_perfume_be.member.dto.MemberResponse
import ym_cosmetic.pick_perfume_be.member.dto.SignupRequest
import ym_cosmetic.pick_perfume_be.member.dto.UpdateMemberRequest
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.security.CurrentMember


@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberService: MemberService
) {

    @GetMapping("/me")
    fun getCurrentMember(@CurrentMember currentMember: Member): ApiResponse<MemberResponse> {
        return ApiResponse.success(MemberResponse.from(currentMember))
    }

    @PostMapping()
    fun createMember(@Valid @RequestBody dto: SignupRequest): ApiResponse<MemberResponse> {
        val createdMember = memberService.createMember(dto)
        return ApiResponse.success(MemberResponse.from(createdMember))
    }

    @GetMapping("email/check")
    fun checkEmail(@RequestParam email: String): ApiResponse<Boolean> {
        val isEmailAvailable = memberService.isEmailAvailable(email)
        return ApiResponse.success(isEmailAvailable)
    }

    @GetMapping("nickname/check")
    fun checkNickname(@RequestParam nickname: String): ApiResponse<Boolean> {
        val isNicknameAvailable = memberService.isNicknameAvailable(nickname)
        return ApiResponse.success(isNicknameAvailable)
    }

    @PutMapping("/me")
    fun updateMember(
        @CurrentMember currentMember: Member,
        @Valid @RequestBody dto: UpdateMemberRequest
    ): ApiResponse<MemberResponse> {
        val updatedMember = memberService.updateMember(currentMember, dto)
        return ApiResponse.success(MemberResponse.from(updatedMember))
    }

    @GetMapping("/presigned-url")
    fun getPresignedUrl(
        @CurrentMember member: Member,
        @RequestParam("fileKey") fileKey: String,
    ): ApiResponse<PresignedUrlResponse> {
        val presignedUrl = memberService.getPresignedUrl(member.id!!, fileKey)
        return ApiResponse.success("임시 URL이 생성되었습니다", presignedUrl)
    }
}