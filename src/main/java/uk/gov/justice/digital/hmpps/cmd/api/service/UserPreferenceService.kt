package uk.gov.justice.digital.hmpps.cmd.api.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference
import uk.gov.justice.digital.hmpps.cmd.api.dto.UserPreferenceDto
import uk.gov.justice.digital.hmpps.cmd.api.model.UserPreference
import uk.gov.justice.digital.hmpps.cmd.api.repository.UserPreferenceRepository
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.time.LocalDate

@Service
class UserPreferenceService(
  private val repository: UserPreferenceRepository,
  private val authenticationFacade: HmppsAuthenticationHolder,
) {

  fun getUserPreference(): UserPreferenceDto = UserPreferenceDto.from(getUserPreference(authenticationFacade.username!!))
    ?: throw NotFoundException("Preferences not found for ${authenticationFacade.username}")

  fun updateSnoozePreference(newDate: LocalDate) {
    val userPreferences = getOrCreateUserPreference()
    log.debug("Updating snooze preference for user ${userPreferences.quantumId} (${userPreferences.snoozeUntil}) with $newDate")
    userPreferences.snoozeUntil = newDate
    log.info("Updated snooze preference for user ${userPreferences.quantumId} (${userPreferences.snoozeUntil})")
    repository.save(userPreferences)
  }

  fun updateNotificationDetails(email: String, sms: String?, communicationPreference: CommunicationPreference) {
    val userPreferences = getOrCreateUserPreference()
    log.debug("Updating notification preference for user ${userPreferences.quantumId} to email: $email, sms: $sms, preference: $communicationPreference")
    userPreferences.email = email
    userPreferences.sms = sms
    userPreferences.commPref = communicationPreference
    log.info("Updated snooze preference for user ${userPreferences.quantumId}")
    repository.save(userPreferences)
  }

  fun getOrCreateUserPreference(quantumId: String = authenticationFacade.username!!): UserPreference = getUserPreference(quantumId) ?: repository.save(UserPreference(quantumId.uppercase()))
    .also { log.info("getOrCreateUserPreference: Created new user preference for $quantumId") }

  fun getUserPreference(quantumId: String): UserPreference? {
    val userPreference = repository.findByQuantumIdIgnoreCase(quantumId)
    log.debug("getUserPreference: Finding user preference for $quantumId")
    return if (userPreference != null) {
      log.info("Found user preference for $quantumId")
      log.debug(userPreference.toString())
      userPreference
    } else {
      log.info("User preference for $quantumId not found")
      null
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(UserPreferenceService::class.java)
  }
}

class NotFoundException(message: String) : RuntimeException(message)
