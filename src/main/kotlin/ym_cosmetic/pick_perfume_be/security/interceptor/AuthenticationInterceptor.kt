package ym_cosmetic.pick_perfume_be.security.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.servlet.HandlerInterceptor
import ym_cosmetic.pick_perfume_be.member.MemberService
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.security.MemberContext

@Component
class AuthenticationInterceptor(
    private val memberContext: MemberContext,
    private val memberService: MemberService
) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest, response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val session = request.getSession(false)
        if (session != null) {
            val memberId = session.getAttribute(MEMBER_ID_SESSION_KEY) as Long?
            if (memberId != null) {
                try {
                    val member: Member = memberService.findById(memberId)
                    memberContext.setCurrentMember(member)
                } catch (e: Exception) {
                    clearMemberFromSession(request)
                    System.err.println("사용자 조회 실패: " + e.message)
                }
            }
        }

        return true
    }

    fun setMemberToSession(request: HttpServletRequest, memberId: Long?) {
        val session = request.getSession(true)
        session.setAttribute(MEMBER_ID_SESSION_KEY, memberId)
    }

    val memberSession: HttpSession?
        get() {
            val attr = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?
            return attr?.request?.getSession(false)
        }

    fun clearMemberFromSession(request: HttpServletRequest) {
        val session = request.getSession(false)
        if (session != null) {
            session.removeAttribute(MEMBER_ID_SESSION_KEY)
            session.invalidate()
        }
    }

    companion object {
        const val MEMBER_ID_SESSION_KEY = "MEMBER_ID"
    }
}