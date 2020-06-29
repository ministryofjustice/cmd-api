package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto.PreferenceDto
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.SnoozePreference
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.repository.SnoozePreferenceRepository
import java.time.Clock
import java.time.LocalDate

@Service
@Transactional
class SnoozeNotificationPreferenceService(@Autowired val repository: SnoozePreferenceRepository, @Autowired val clock: Clock, @Autowired val authenticationFacade: AuthenticationFacade) {
    fun getSnoozePreference(): PreferenceDto {
        val quantumId = authenticationFacade.currentUsername
        val preference = repository.findByQuantumIdAndSnoozeGreaterThanEqual(quantumId, LocalDate.now(clock))
        return if (preference != null) {
            log.debug("Found snooze preference for user ${preference.quantumId} (${preference.snooze})")
            PreferenceDto.from(preference)
        } else {
            log.debug("Found no notification for user $quantumId")
            PreferenceDto(null)
        }
    }

    fun createOrUpdateSnoozePreference(newDate: LocalDate) {
        val quantumId = authenticationFacade.currentUsername
        val preference = repository.findByQuantumIdAndSnoozeGreaterThanEqual(quantumId, LocalDate.now(clock))
        if (preference != null) {
            preference.snooze = newDate
            repository.save(preference)
            log.debug("Updated snooze preference for user ${preference.quantumId} (${preference.snooze})")
        } else {
            val newPreference = SnoozePreference(quantumId, newDate)
            repository.save(newPreference)
            log.debug("Created snooze preference for user ${newPreference.quantumId} (${newPreference.snooze})")
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SnoozeNotificationPreferenceService::class.java)
    }
}