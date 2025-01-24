package uk.gov.justice.digital.hmpps.cmd.api.client

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class PrisonApiClient(@Qualifier("prisonApiWebClient") private val webClient: WebClient) {

  fun getCurrentPrisonIdForUser(): String = webClient
    .get()
    .uri("/api/users/me")
    .retrieve()
    .bodyToMono(CaseLoad::class.java)
    .block()?.activeCaseLoadId ?: ""
}

data class CaseLoad(
  @JsonProperty("activeCaseLoadId")
  val activeCaseLoadId: String?,
)
