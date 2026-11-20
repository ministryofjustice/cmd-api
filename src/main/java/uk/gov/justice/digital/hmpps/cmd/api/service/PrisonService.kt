package uk.gov.justice.digital.hmpps.cmd.api.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.cmd.api.client.ManageUsersApiClient
import uk.gov.justice.digital.hmpps.cmd.api.repository.PrisonRepository
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder

@Service
class PrisonService(
  private val repository: PrisonRepository,
  private val manageUsersApiClient: ManageUsersApiClient,
  private val authenticationFacade: HmppsAuthenticationHolder,
) {
  fun getPrisonAndRegionForUser(): Pair<String, Int?> {
    val username = authenticationFacade.username!!
    val prisonId = manageUsersApiClient.getCurrentPrisonIdForUser(username)
    val prison = repository.findByPrisonId(prisonId)
    if (prison == null) {
      log.warn("No prison found in db for prisonId=$prisonId, user=$username")
      return prisonId to null
    }
    return prisonId to prison.region
  }

  companion object {
    private val log = LoggerFactory.getLogger(PrisonService::class.java)
  }
}
