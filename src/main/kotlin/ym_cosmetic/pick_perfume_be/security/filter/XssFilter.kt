package ym_cosmetic.pick_perfume_be.security.filter

import jakarta.servlet.*
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import ym_cosmetic.pick_perfume_be.security.XssRequestWrapper
import java.io.IOException

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class XssFilter : Filter {
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(
        request: ServletRequest?,
        response: ServletResponse?,
        chain: FilterChain
    ) {
        chain.doFilter(XssRequestWrapper(request as HttpServletRequest?), response)
    }
}