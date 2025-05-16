package ym_cosmetic.pick_perfume_be.community.dto.response

import ym_cosmetic.pick_perfume_be.member.entity.Member

data class PostAuthorResponse(
    val id: Long,
    val nickname: String,
    val profileImage: String?
) {
    companion object {
        fun from(member: Member): PostAuthorResponse {
            return PostAuthorResponse(
                id = member.id!!,
                nickname = member.nickname,
                profileImage = member.profileImage?.url
            )
        }
    }
} 