package ym_cosmetic.pick_perfume_be.member.dto

import ym_cosmetic.pick_perfume_be.member.entity.MemberPreference
import java.time.LocalDateTime

data class MemberPreferenceDto(
    val memberId: Long,
    val preferredNotes: List<String>,
    val preferredAccords: List<String>,
    val preferredBrands: List<String>,
    val reviewedPerfumeIds: List<Long>,
    val lastUpdated: LocalDateTime
) {
    companion object {
        fun from(memberPreference: MemberPreference): MemberPreferenceDto {
            return MemberPreferenceDto(
                memberId = memberPreference.memberId,
                preferredNotes = memberPreference.preferredNotes,
                preferredAccords = memberPreference.preferredAccords,
                preferredBrands = memberPreference.preferredBrands,
                reviewedPerfumeIds = memberPreference.reviewedPerfumeIds,
                lastUpdated = memberPreference.lastUpdated
            )
        }
    }
}