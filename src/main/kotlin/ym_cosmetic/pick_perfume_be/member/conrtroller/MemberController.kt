package ym_cosmetic.pick_perfume_be.member.conrtroller

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import ym_cosmetic.pick_perfume_be.common.dto.response.ApiResponse
import ym_cosmetic.pick_perfume_be.member.MemberService
import ym_cosmetic.pick_perfume_be.member.dto.MemberResponse
import ym_cosmetic.pick_perfume_be.member.dto.SignupRequest
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
}