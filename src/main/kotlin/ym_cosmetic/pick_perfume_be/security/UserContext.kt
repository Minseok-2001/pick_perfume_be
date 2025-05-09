package ym_cosmetic.pick_perfume_be.security

import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import org.springframework.web.context.WebApplicationContext
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.member.enums.UserRole

@Component
@Scope(
    value = WebApplicationContext.SCOPE_REQUEST,
    proxyMode = ScopedProxyMode.TARGET_CLASS
)
class UserContext {
    var currentUser: Member? = null

    val isAuthenticated: Boolean
        get() = currentUser != null

    val userRole: UserRole?
        get() = currentUser?.role

    fun setCurrentUser(user: Member) {
        this.currentUser = user
    }

    fun isAuthenticated(): Boolean {
        return currentUser != null
    }

    fun getUserRole(): UserRole? {
        return currentUser?.role
    }
}