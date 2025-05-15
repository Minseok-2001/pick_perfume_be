package ym_cosmetic.pick_perfume_be.security.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.servlet.HandlerInterceptor
import ym_cosmetic.pick_perfume_be.common.exception.UnauthorizedException
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
            // 세션이 강제로 만료되었는지 확인
            if (session.getAttribute("expired") == true) {
                clearMemberFromSession(request)
                throw UnauthorizedException("다른 위치에서 로그인하여 세션이 만료되었습니다.")
            }
            
            val memberId = session.getAttribute(MEMBER_ID_SESSION_KEY) as Long?
            if (memberId != null) {
                try {
                    val member: Member = memberService.findById(memberId)
                    memberContext.setCurrentMember(member)
                    
                    // 세션 활성화 시간 갱신
                    session.setAttribute(LAST_ACCESS_TIME_KEY, System.currentTimeMillis())
                } catch (e: Exception) {
                    // 사용자를 찾을 수 없는 경우 세션 정리
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
        session.setAttribute(LAST_ACCESS_TIME_KEY, System.currentTimeMillis())
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
            session.removeAttribute(LAST_ACCESS_TIME_KEY)
            session.invalidate()
        }
    }

    companion object {
        const val MEMBER_ID_SESSION_KEY = "MEMBER_ID"
        const val LAST_ACCESS_TIME_KEY = "LAST_ACCESS_TIME"
    }
}