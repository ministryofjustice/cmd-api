package uk.gov.justice.digital.hmpps.cmd.api.utils

import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.io.IOException
import javax.servlet.*
import javax.servlet.http.HttpServletRequest

@Component
@Order(4)
class UserContextFilter : Filter {
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        val httpServletRequest = servletRequest as HttpServletRequest
        val authToken: String? = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)
        val authentication = SecurityContextHolder.getContext().authentication
        UserContext.setAuthToken(authToken)
        UserContext.setAuthentication(authentication)

        filterChain.doFilter(httpServletRequest, servletResponse)
    }

    override fun init(filterConfig: FilterConfig) {}
    override fun destroy() {}
}