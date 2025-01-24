package uk.gov.justice.digital.hmpps.cmd.api.utils

import org.springframework.security.core.Authentication

object UserContext {
  private val authToken = ThreadLocal<String>()
  private val authentication = ThreadLocal<Authentication>()

  fun getAuthToken(): String = authToken.get()

  fun setAuthToken(aToken: String?) {
    authToken.set(aToken)
  }

  fun getAuthentication(): Authentication = authentication.get()

  fun setAuthentication(auth: Authentication?) {
    authentication.set(auth)
  }
}
