package uk.gov.justice.digital.hmpps.cmd.api.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper

@Component
class EntityWithJwtAuthorisationBuilder(@Autowired val jwtAuthenticationHelper: JwtAuthorisationHelper) {

  fun entityWithJwtAuthorisation(user: String, roles: List<String>, body: Any): HttpEntity<*> {
    val headers = addCommonHeaders(user, roles)
    return HttpEntity(body, headers)
  }

  fun entityWithJwtAuthorisation(user: String, roles: List<String>): HttpEntity<*> = HttpEntity<Any>(null, addCommonHeaders(user, roles))

  private fun addCommonHeaders(user: String, roles: List<String>): HttpHeaders = HttpHeaders().apply {
    jwtAuthenticationHelper.setAuthorisationHeader(username = user, roles = roles, scope = listOf("read", "write"))(this)
    this.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
  }
}
