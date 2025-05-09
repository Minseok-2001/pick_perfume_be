package ym_cosmetic.pick_perfume_be.security.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.servlet.HandlerInterceptor
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.security.UserContext

@Component
@RequiredArgsConstructor
class AuthenticationInterceptor : HandlerInterceptor {
    private val userContext: UserContext? = null

    private val userService: UserService? = null

    override fun preHandle(
        request: HttpServletRequest, response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val session = request.getSession(false)
        if (session != null) {
            val userId = session.getAttribute(USER_ID_SESSION_KEY) as Long?
            if (userId != null) {
                try {
                    val user: Member? = userService.findById(userId)
                    if (user != null) {
                        userContext?.setCurrentUser(user)
                    }
                } catch (e: Exception) {
                    System.err.println("사용자 조회 실패: " + e.message)
                }
            }
        }

        return true
    }

    fun setUserToSession(request: HttpServletRequest, userId: Long?) {
        val session = request.getSession(true)
        session.setAttribute(USER_ID_SESSION_KEY, userId)
    }

    val userSession: HttpSession?
        get() {
            val attr = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?
            if (attr == null) {
                return null
            }

            val request = attr.request
            return request.getSession(false)
        }

    fun clearUserFromSession(request: HttpServletRequest) {
        val session = request.getSession(false)
        if (session != null) {
            session.removeAttribute(USER_ID_SESSION_KEY)
            session.invalidate()
        }
    }

    companion object {
        private const val USER_ID_SESSION_KEY = "USER_ID"
    }
}