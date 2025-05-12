package ym_cosmetic.pick_perfume_be.security

import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import org.springframework.web.context.WebApplicationContext
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.member.enums.MemberRole

@Component
@Scope(
    value = WebApplicationContext.SCOPE_REQUEST,
    proxyMode = ScopedProxyMode.TARGET_CLASS
)
class MemberContext {
    private var _currentMember: Member? = null

    val currentMember: Member?
        get() = _currentMember

    val isAuthenticated: Boolean
        get() = _currentMember != null

    val memberRole: MemberRole?
        get() = _currentMember?.memberRole

    fun setCurrentMember(member: Member) {
        this._currentMember = member
    }
}