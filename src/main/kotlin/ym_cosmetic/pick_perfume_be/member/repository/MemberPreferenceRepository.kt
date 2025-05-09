package ym_cosmetic.pick_perfume_be.member.repository

import org.springframework.data.jpa.repository.JpaRepository
import ym_cosmetic.pick_perfume_be.member.entity.MemberPreference

interface MemberPreferenceRepository : JpaRepository<MemberPreference, Long> {
    fun findByMemberId(memberId: Long): MemberPreference?
}