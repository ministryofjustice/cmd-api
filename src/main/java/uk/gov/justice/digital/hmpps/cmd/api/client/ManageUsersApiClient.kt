package uk.gov.justice.digital.hmpps.cmd.api.client

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class ManageUsersApiClient(@Qualifier("manageUsersApiWebClient") private val restClient: RestClient) {

  fun getCurrentPrisonIdForUser(username: String): String = restClient
    .get()
    .uri("/prisonusers/{username}/details", username)
    .retrieve()
    .body<PrisonUserDetails>()
    ?.activeCaseloadId ?: ""
}

data class PrisonUserDetails(
  val activeCaseloadId: String?,
)
