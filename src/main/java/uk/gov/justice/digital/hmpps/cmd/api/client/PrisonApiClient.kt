package uk.gov.justice.digital.hmpps.cmd.api.client

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class PrisonApiClient(@Qualifier("prisonApiWebClient") private val restClient: RestClient) {

  fun getCurrentPrisonIdForUser(): String = restClient
    .get()
    .uri("/api/users/me")
    .retrieve()
    .body(CaseLoad::class.java)
    ?.activeCaseLoadId ?: ""
}

data class CaseLoad(
  @JsonProperty("activeCaseLoadId")
  val activeCaseLoadId: String?,
)
