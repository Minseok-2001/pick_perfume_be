package ym_cosmetic.pick_perfume_be.auth.service

import jakarta.servlet.http.HttpSession
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import ym_cosmetic.pick_perfume_be.auth.dto.ChangePasswordRequest
import ym_cosmetic.pick_perfume_be.auth.dto.LoginRequest
import ym_cosmetic.pick_perfume_be.auth.dto.LoginResponse
import ym_cosmetic.pick_perfume_be.auth.dto.ResetPasswordRequest
import ym_cosmetic.pick_perfume_be.common.exception.EntityNotFoundException
import ym_cosmetic.pick_perfume_be.common.exception.UnauthorizedException
import ym_cosmetic.pick_perfume_be.member.MemberService
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.member.entity.MemberResetToken
import ym_cosmetic.pick_perfume_be.member.repository.MemberRepository
import ym_cosmetic.pick_perfume_be.member.repository.MemberResetTokenRepository
import ym_cosmetic.pick_perfume_be.security.PasswordEncoder
import ym_cosmetic.pick_perfume_be.security.config.SessionConfig
import ym_cosmetic.pick_perfume_be.security.interceptor.AuthenticationInterceptor
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@Service
class AuthService(
    private val memberService: MemberService,
    private val session: HttpSession,
    private val memberRepository: MemberRepository,
    private val memberResetTokenRepository: MemberResetTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationInterceptor: AuthenticationInterceptor,
    private val emailService: EmailService,
) {

    @Value("\${app.domainUrl}")
    private lateinit var domainUrl: String

    companion object {
        const val USER_SESSION_KEY = "USER_ID"
        const val TOKEN_EXPIRY_HOURS = 1L
    }

    fun login(dto: LoginRequest): LoginResponse? {
        val member = memberRepository.findByEmail(dto.email)
            ?: throw IllegalArgumentException("User not found with email: ${dto.email}")
        if (!member.isCredentialValid(dto.password, passwordEncoder)) {
            throw IllegalArgumentException("Invalid password")
        }

        SessionConfig.removeConcurrentSession(member.id ?: throw IllegalArgumentException("Invalid session ID"))

        val httpRequest =
            (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        authenticationInterceptor.setMemberToSession(httpRequest, member.id)

        return LoginResponse.from(member)
    }

    fun logout() {
        val httpRequest =
            (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        authenticationInterceptor.clearMemberFromSession(httpRequest)
    }

    @Transactional
    fun changePassword(request: ChangePasswordRequest): Boolean {
        val httpRequest = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        val userId = httpRequest.session.getAttribute(USER_SESSION_KEY) as Long?
            ?: throw UnauthorizedException("로그인 후 이용해주세요.")

        val member = memberRepository.findById(userId).orElseThrow {
            EntityNotFoundException("해당 사용자를 찾을 수 없습니다.")
        }

        // 현재 비밀번호 확인
        if (!member.isCredentialValid(request.currentPassword, passwordEncoder)) {
            throw UnauthorizedException("현재 비밀번호가 일치하지 않습니다.")
        }

        // 새 비밀번호와 확인 비밀번호 일치 여부 확인
        if (request.newPassword != request.confirmPassword) {
            throw UnauthorizedException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.")
        }

        // 비밀번호 업데이트
        updateMemberPassword(member, request.newPassword)
        return true
    }

    @Transactional
    fun sendPasswordResetEmail(email: String) {
        val member = memberRepository.findByEmail(email)
            ?: throw UnauthorizedException("해당 이메일로 가입된 계정이 없습니다.")

        // 기존 토큰이 있으면 모두 만료 처리
        invalidateExistingTokens(email)

        // 토큰 생성
        val tokenString = UUID.randomUUID().toString()
        val expiryTime = LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS)
        val expiryTimestamp = expiryTime.toEpochSecond(ZoneOffset.UTC)

        // 토큰 저장
        val resetToken = MemberResetToken(
            token = tokenString,
            member = member,
            email = email,
            expirationDate = expiryTimestamp,
            isUsed = false
        )
        memberResetTokenRepository.save(resetToken)

        val resetLink = "$domainUrl/reset-password?token=$tokenString"
        emailService.sendPasswordResetEmail(email, resetLink)
    }

    @Transactional
    fun resetPassword(request: ResetPasswordRequest) {
        // 토큰 유효성 확인
        val resetToken = memberResetTokenRepository.findById(request.token)
            .orElseThrow { UnauthorizedException("유효하지 않은 토큰입니다.") }

        val currentTimestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)

        // 토큰 유효성 검증
        if (resetToken.isUsed) {
            throw UnauthorizedException("이미 사용된 토큰입니다.")
        }

        if (currentTimestamp > resetToken.expirationDate) {
            throw UnauthorizedException("만료된 토큰입니다.")
        }

        // 비밀번호 확인
        if (request.newPassword != request.confirmPassword) {
            throw UnauthorizedException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.")
        }

        // 회원 조회
        val member = resetToken.member

        // 비밀번호 변경
        updateMemberPassword(member, request.newPassword)

        // 토큰 사용 처리
        resetToken.isUsed = true
        memberResetTokenRepository.save(resetToken)
    }

    private fun updateMemberPassword(member: Member, newPassword: String) {
        member.password = passwordEncoder.encode(newPassword)
        memberRepository.save(member)
    }

    /**
     * 기존에 발급된 사용되지 않은 토큰을 모두 만료 처리
     */
    private fun invalidateExistingTokens(email: String) {
        val tokens = memberResetTokenRepository.findByEmailAndIsUsedFalse(email)
        tokens.forEach {
            it.isUsed = true
            memberResetTokenRepository.save(it)
        }
    }
}