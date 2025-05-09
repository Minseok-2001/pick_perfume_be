package ym_cosmetic.pick_perfume_be.security.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import ym_cosmetic.pick_perfume_be.member.enums.UserRole
import ym_cosmetic.pick_perfume_be.security.RequireRole
import ym_cosmetic.pick_perfume_be.security.UserContext

@Component
class RoleCheckInterceptor(
    private val userContext: UserContext
) : HandlerInterceptor {

    @Throws(Exception::class)
    override fun preHandle(
        request: HttpServletRequest, response: HttpServletResponse,
        handler: Any
    ): Boolean {
        if (handler !is HandlerMethod) {
            return true
        }

        val requireRole: RequireRole? = handler.getMethodAnnotation(RequireRole::class.java)

        if (requireRole == null) {
            return true
        }

        if (!userContext.isAuthenticated()) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.writer.write("인증이 필요합니다.")
            return false
        }

        val userRole: UserRole? = userContext.getUserRole()
        for (role in requireRole.value) {
            if (role === userRole) {
                return true
            }
        }

        response.status = HttpServletResponse.SC_FORBIDDEN
        response.writer.write("해당 기능에 접근 권한이 없습니다.")
        return false
    }
}