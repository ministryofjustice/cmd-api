package uk.gov.justice.digital.hmpps.cmd.api.utils

import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.cmd.api.security.UserSecurityUtils
import java.io.IOException
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

@Component
@Order(1)
class UserMdcFilter @Autowired constructor(private val userSecurityUtils: UserSecurityUtils) : Filter {
    override fun init(filterConfig: FilterConfig) {
        // Initialise - no functionality
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val currentUsername = userSecurityUtils.currentUsername
        try {
            MDC.put(USER_ID_HEADER, currentUsername)
            chain.doFilter(request, response)
        } finally {
            MDC.remove(USER_ID_HEADER)
        }
    }

    override fun destroy() {
        // Destroy - no functionality
    }

    companion object {
        private const val USER_ID_HEADER = "userId"
    }

}