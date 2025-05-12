package ym_cosmetic.pick_perfume_be.member.dto

import ym_cosmetic.pick_perfume_be.member.entity.Member

/**
 * 회원 요약 정보 DTO
 */
data class MemberSummaryDto(
    val id: Long,
    val name: String,
    val nickname: String?,
    val profileImageUrl: String?
) {
    companion object {
        fun from(member: Member): MemberSummaryDto {
            return MemberSummaryDto(
                id = member.id!!,
                name = member.name,
                nickname = member.nickname,
                profileImageUrl = member.profileImageUrl
            )
        }
    }
} 