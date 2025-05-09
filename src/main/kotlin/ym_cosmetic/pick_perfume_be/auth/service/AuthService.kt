package ym_cosmetic.pick_perfume_be.auth.service

import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import ym_cosmetic.pick_perfume_be.auth.dto.LoginRequest
import ym_cosmetic.pick_perfume_be.auth.dto.LoginResponse
import ym_cosmetic.pick_perfume_be.member.MemberService
import ym_cosmetic.pick_perfume_be.member.repository.MemberRepository
import ym_cosmetic.pick_perfume_be.security.PasswordEncoder
import ym_cosmetic.pick_perfume_be.security.interceptor.AuthenticationInterceptor

@Service
class AuthService(
    private val memberService: MemberService,
    private val session: HttpSession,
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationInterceptor: AuthenticationInterceptor,
) {

    companion object {
        const val USER_SESSION_KEY = "USER_ID"
    }

    fun login(dto: LoginRequest): LoginResponse? {

        val member = memberRepository.findByEmail(dto.email)
            ?: throw IllegalArgumentException("User not found with email: ${dto.email}")
        if (!member.isCredentialValid(dto.password, passwordEncoder)) {
            throw IllegalArgumentException("Invalid password")
        }

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

}