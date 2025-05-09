package ym_cosmetic.pick_perfume_be.member

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ym_cosmetic.pick_perfume_be.common.exception.EntityNotFoundException
import ym_cosmetic.pick_perfume_be.member.dto.MemberResponse
import ym_cosmetic.pick_perfume_be.member.dto.SignupRequest
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.member.repository.MemberRepository
import ym_cosmetic.pick_perfume_be.security.PasswordEncoder

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder
) {


    @Transactional(readOnly = true)
    fun findById(memberId: Long): Member {
        return memberRepository.findById(memberId).orElseThrow {
            throw EntityNotFoundException("Member not found with id: $memberId")
        }
    }

    @Transactional
    fun createMember(dto: SignupRequest): MemberResponse {
        val member = Member(
            nickname = dto.nickname,
            name = dto.name,
            password = dto.password?.let { passwordEncoder.encode(it) },
            email = dto.email,
            profileImage = dto.profileImage
        )
        val savedMember = memberRepository.save(member)
        return MemberResponse.from(savedMember)
    }
}