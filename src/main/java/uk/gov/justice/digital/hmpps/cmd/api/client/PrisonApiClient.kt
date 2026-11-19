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

/* manage users endpoint  /users/me/caseloads
returns:
{
  "username": "TESTUSER1",
  "active": true,
  "accountType": "GENERAL",
  "activeCaseload": "BXI",
  "caseloads": [
  {
    "id": "WWI",
    "name": "WANDSWORTH (HMP)",
    "function": "GENERAL"
  }
  ]
}
*/
data class UserCaseloadDetail(
  val username: String,
  val active: Boolean,
  val accountType: UsageType = UsageType.GENERAL,
  val activeCaseload: PrisonCaseload?,
  val caseloads: List<PrisonCaseload> = listOf(),
)

data class PrisonCaseload(
  val id: String,
  val name: String,
  val function: String,
)

enum class UsageType {
  GENERAL,
  ADMIN,
}
