package ym_cosmetic.pick_perfume_be.member

import org.springframework.aop.support.AopUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronizationManager
import ym_cosmetic.pick_perfume_be.common.exception.EntityNotFoundException
import ym_cosmetic.pick_perfume_be.community.dto.response.PresignedUrlResponse
import ym_cosmetic.pick_perfume_be.infrastructure.r2.R2Service
import ym_cosmetic.pick_perfume_be.member.dto.SignupRequest
import ym_cosmetic.pick_perfume_be.member.dto.UpdateMemberRequest
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.member.repository.MemberRepository
import ym_cosmetic.pick_perfume_be.security.PasswordEncoder

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val r2Service: R2Service
) {

    @Transactional(readOnly = true)
    fun findById(memberId: Long): Member {
        return memberRepository.findById(memberId).orElseThrow {
            throw EntityNotFoundException("Member not found with id: $memberId")
        }
    }

    @Transactional
    fun createMember(dto: SignupRequest): Member {
        println(AopUtils.isAopProxy(memberRepository))
        println("트랜잭션 활성화 여부: ${TransactionSynchronizationManager.isActualTransactionActive()}")
        val member = Member(
            nickname = dto.nickname,
            name = dto.name,
            password = dto.password?.let { passwordEncoder.encode(it) },
            email = dto.email,
            profileImage = dto.profileImage
        )
        val savedMember = memberRepository.save(member)
        return savedMember
    }

    @Transactional(readOnly = true)
    fun isEmailAvailable(email: String): Boolean {
        return !memberRepository.existsByEmail(email)
    }

    @Transactional(readOnly = true)
    fun isNicknameAvailable(nickname: String): Boolean {
        return !memberRepository.existsByNickname(nickname)
    }

    @Transactional
    fun updateMember(currentMember: Member, dto: UpdateMemberRequest): Member {
        val member = findById(currentMember.id!!)
        member.nickname = dto.nickname ?: member.nickname
        member.name = dto.name
        member.profileImage = dto.profileImage

        return memberRepository.save(member)
    }

    fun getPresignedUrl(memberId: Long, fileName: String): PresignedUrlResponse {
        val dirPath = "users/$memberId"
        return r2Service.createPresignedUrl(dirPath, fileName)
    }
}