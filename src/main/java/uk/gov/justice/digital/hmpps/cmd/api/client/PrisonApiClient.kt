package uk.gov.justice.digital.hmpps.cmd.api.client

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class PrisonApiClient(@Qualifier("manageUsersApiWebClient") private val restClient: RestClient) {

  fun getCurrentPrisonIdForUser(): String = restClient
    .get()
    .uri("/users/me/caseloads")
    .retrieve()
    .body<UserCaseloadDetail>()
    ?.activeCaseload?.id ?: ""
}

data class UserCaseloadDetail(
  val activeCaseload: PrisonCaseload?,
)

data class PrisonCaseload(
  val id: String,
)
