package uk.gov.justice.digital.hmpps.cmd.api.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.cmd.api.dto.UserPreferenceDto
import uk.gov.justice.digital.hmpps.cmd.api.model.UserPreference
import uk.gov.justice.digital.hmpps.cmd.api.repository.UserPreferenceRepository
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import java.time.Clock
import java.time.LocalDate

@Service
@Transactional
class UserPreferenceService(@Autowired val repository: UserPreferenceRepository, @Autowired val clock: Clock, @Autowired val authenticationFacade: AuthenticationFacade) {

    fun getUserPreference(): UserPreferenceDto {
        val quantumId = authenticationFacade.currentUsername
        val userPreferences = repository.findByQuantumIdAndSnoozeUntilGreaterThanEqual(quantumId, LocalDate.now(clock))
        return if (userPreferences != null) {
            log.debug("Found snooze preference for user ${userPreferences.quantumId} (${userPreferences.snoozeUntil})")
            UserPreferenceDto.from(userPreferences)
        } else {
            log.debug("Found no snooze preference for user $quantumId")
            UserPreferenceDto(null)
        }
    }

    fun createOrUpdateUserPreference(newDate: LocalDate) {
        val quantumId = authenticationFacade.currentUsername
        val userPreferences = repository.findByQuantumId(quantumId)
        if (userPreferences != null) {
            userPreferences.snoozeUntil = newDate
            repository.save(userPreferences)
            log.debug("Updated snooze preference for user ${userPreferences.quantumId} (${userPreferences.snoozeUntil})")
        } else {
            val newPreference = UserPreference(quantumId, newDate)
            repository.save(newPreference)
            log.debug("Created snooze preference for user ${newPreference.quantumId} (${newPreference.snoozeUntil})")
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(UserPreferenceService::class.java)
    }
}