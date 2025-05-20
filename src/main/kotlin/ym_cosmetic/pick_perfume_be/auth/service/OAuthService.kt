package ym_cosmetic.pick_perfume_be.auth.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import ym_cosmetic.pick_perfume_be.auth.dto.KakaoUserInfo
import ym_cosmetic.pick_perfume_be.auth.dto.LoginResponse
import ym_cosmetic.pick_perfume_be.common.vo.ImageUrl
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.member.enums.AuthProvider
import ym_cosmetic.pick_perfume_be.member.enums.MemberRole
import ym_cosmetic.pick_perfume_be.member.repository.MemberRepository
import ym_cosmetic.pick_perfume_be.security.config.SessionConfig
import ym_cosmetic.pick_perfume_be.security.interceptor.AuthenticationInterceptor
import java.util.*

@Service
class OAuthService(
    private val memberRepository: MemberRepository,
    private val authenticationInterceptor: AuthenticationInterceptor,
    private val emailService: EmailService

) {
    
    @Transactional
    fun loginKakaoUser(userInfo: KakaoUserInfo): LoginResponse {
        val kakaoId = userInfo.id.toString()
        val email = userInfo.kakaoAccount?.email ?: "${kakaoId}@kakao.com"
        
        // 기존 회원 조회
        val existingMember = memberRepository.findByProviderId(kakaoId)
            ?: memberRepository.findByEmail(email)
            ?: createKakaoUser(userInfo)
            
        // 기존 회원이 로컬 계정이고 카카오 연동이 안 되어있으면 연동
        if (existingMember.provider == AuthProvider.LOCAL && existingMember.providerId == null) {
            existingMember.provider = AuthProvider.KAKAO
            existingMember.providerId = kakaoId
            memberRepository.save(existingMember)
        }
        
        // 세션 관리
        SessionConfig.removeConcurrentSession(existingMember.id!!)
        
        val httpRequest = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        authenticationInterceptor.setMemberToSession(httpRequest, existingMember.id)
        
        return LoginResponse.from(existingMember)
    }
    
    private fun createKakaoUser(userInfo: KakaoUserInfo): Member {
        val kakaoId = userInfo.id.toString()
        val email = userInfo.kakaoAccount?.email ?: "${kakaoId}@kakao.com"
        val name = userInfo.kakaoAccount?.name
        val nickname = userInfo.properties?.nickname ?: "사용자${UUID.randomUUID().toString().substring(0, 8)}"
        val profileImage = userInfo.properties?.profileImage?.let { ImageUrl(it) }
        
        val member = Member(
            email = email,
            nickname = nickname,
            name = name,
            profileImage = profileImage,
            memberRole = MemberRole.MEMBER,
            provider = AuthProvider.KAKAO,
            providerId = kakaoId
        )
        
        return memberRepository.save(member)
    }
}