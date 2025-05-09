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

@Component
class CurrentUserArgumentResolver(userService: UserService) : HandlerMethodArgumentResolver {
    private val userService: UserService

    init {
        this.userService = userService
    }

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation<A?>(CurrentUser::class.java) &&
                (parameter.getParameterType() == User::class.java ||
                        parameter.getParameterType() == Long::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter, mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest, binderFactory: WebDataBinderFactory?
    ): Any? {
        val userId = RequestContextHolder.currentRequestAttributes()
            .getAttribute(USER_ID_SESSION_KEY, RequestAttributes.SCOPE_SESSION) as Long?

        if (userId == null) {
            if (parameter.getParameterType() == Long::class.java) {
                return null
            }
            throw UnauthorizedException("로그인이 필요합니다.")
        }

        if (parameter.getParameterType() == Long::class.java) {
            return userId
        }

        return userService.findById(userId)
    }

    companion object {
        private const val USER_ID_SESSION_KEY = "USER_ID"
    }
}