package ym_cosmetic.pick_perfume_be.security.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import ym_cosmetic.pick_perfume_be.common.exception.ForbiddenException
import ym_cosmetic.pick_perfume_be.common.exception.UnauthorizedException
import ym_cosmetic.pick_perfume_be.member.enums.MemberRole
import ym_cosmetic.pick_perfume_be.security.MemberContext
import ym_cosmetic.pick_perfume_be.security.RequireRole

@Component
class RoleCheckInterceptor(
    private val memberContext: MemberContext
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

        if (!memberContext.isAuthenticated) {
            throw UnauthorizedException("로그인이 필요합니다.")
        }

        val memberRole: MemberRole? = memberContext.memberRole
        for (role in requireRole.value) {
            if (role === memberRole) {
                return true
            }
        }

        throw ForbiddenException("권한이 없습니다.")
    }
}