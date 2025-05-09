package ym_cosmetic.pick_perfume_be.common.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import ym_cosmetic.pick_perfume_be.security.CurrentUserArgumentResolver
import ym_cosmetic.pick_perfume_be.security.interceptor.AuthenticationInterceptor
import ym_cosmetic.pick_perfume_be.security.interceptor.RoleCheckInterceptor

@Configuration
class WebMvcConfig(
    private val authenticationInterceptor: AuthenticationInterceptor,
    private val roleCheckInterceptor: RoleCheckInterceptor,
    private val currentUserArgumentResolver: CurrentUserArgumentResolver
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authenticationInterceptor)
        registry.addInterceptor(roleCheckInterceptor)
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(currentUserArgumentResolver)
    }
}