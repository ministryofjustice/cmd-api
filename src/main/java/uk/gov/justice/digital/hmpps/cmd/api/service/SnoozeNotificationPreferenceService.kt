package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto.SnoozePreferenceDto
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.repository.SnoozePreferenceRepository
import java.time.Clock
import java.time.LocalDate

@Service
@Transactional
class SnoozeNotificationPreferenceService(@Autowired val repository: SnoozePreferenceRepository, @Autowired val clock: Clock, @Autowired val authenticationFacade: AuthenticationFacade) {
    fun getSnoozePreference(): SnoozePreferenceDto {
        val notificationSnoozePreference = repository.findByQuantumIdAndSnoozeGreaterThanEqual(authenticationFacade.currentUsername, LocalDate.now(clock))
        return if (notificationSnoozePreference != null) {
            SnoozePreferenceDto.from(notificationSnoozePreference)
        } else {
            SnoozePreferenceDto(null)
        }
    }
}