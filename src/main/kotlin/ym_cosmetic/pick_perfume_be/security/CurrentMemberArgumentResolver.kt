package ym_cosmetic.pick_perfume_be.security

import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import ym_cosmetic.pick_perfume_be.common.exception.UnauthorizedException
import ym_cosmetic.pick_perfume_be.member.MemberService
import ym_cosmetic.pick_perfume_be.member.entity.Member

@Component
class CurrentMemberArgumentResolver(private val memberService: MemberService) :
    HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(CurrentMember::class.java) &&
                (parameter.getParameterType() == Member::class.java ||
                        parameter.getParameterType() == Long::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter, mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest, binderFactory: WebDataBinderFactory?
    ): Any? {
        val memberId = RequestContextHolder.currentRequestAttributes()
            .getAttribute(MEMBER_ID_SESSION_KEY, RequestAttributes.SCOPE_SESSION) as Long?

        val isOptional = parameter.hasParameterAnnotation(OptionalAuth::class.java)

        if (memberId == null) {
            if (parameter.getParameterType() == Long::class.java || isOptional) {
                return null
            }
            throw UnauthorizedException("로그인이 필요합니다.")
        }

        if (parameter.getParameterType() == Long::class.java) {
            return memberId
        }

        return memberService.findById(memberId)
    }

    companion object {
        private const val MEMBER_ID_SESSION_KEY = "MEMBER_ID"
    }
}
