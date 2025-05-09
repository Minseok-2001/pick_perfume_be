package ym_cosmetic.pick_perfume_be.member

import org.springframework.stereotype.Service
import ym_cosmetic.pick_perfume_be.common.exception.EntityNotFoundException
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.member.repository.MemberRepository

@Service
class MemberService(private val memberRepository: MemberRepository) {

    fun findById(memberId: Long): Member {
        return memberRepository.findById(memberId).orElseThrow {
            throw EntityNotFoundException("Member not found with id: $memberId")
        }
    }
}