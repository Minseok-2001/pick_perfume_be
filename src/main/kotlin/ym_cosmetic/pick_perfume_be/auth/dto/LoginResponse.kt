package ym_cosmetic.pick_perfume_be.auth.dto

import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.member.enums.AuthProvider
import java.time.LocalDateTime

data class LoginResponse(
    val id: Long,
    val nickname: String,
    val email: String,
    val provider: AuthProvider,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(member: Member): LoginResponse {
            return LoginResponse(
                id = member.id!!,
                nickname = member.nickname,
                email = member.email,
                provider = member.provider,
                createdAt = member.createdAt
            )
        }
    }
}
