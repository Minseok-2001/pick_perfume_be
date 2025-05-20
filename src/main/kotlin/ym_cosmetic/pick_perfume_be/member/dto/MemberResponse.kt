package ym_cosmetic.pick_perfume_be.member.dto

import ym_cosmetic.pick_perfume_be.common.vo.ImageUrl
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.member.enums.AuthProvider
import java.time.LocalDateTime

data class MemberResponse(
    val id: Long,
    val email: String,
    val nickname: String,
    val profileImage: ImageUrl?,
    val provider: AuthProvider,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(member: Member): MemberResponse {
            return MemberResponse(
                id = member.id!!,
                email = member.email,
                nickname = member.nickname,
                profileImage = member.profileImage,
                provider = member.provider,
                createdAt = member.createdAt,
            )
        }
    }
}
