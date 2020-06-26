package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto.SnoozePreferenceDto
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.SnoozePreference
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.repository.SnoozePreferenceRepository
import java.time.LocalDate

@Service
@Transactional
class SnoozeNotificationPreferenceService(@Autowired val repository: SnoozePreferenceRepository, @Autowired val date: LocalDate) {
    fun getSnoozePreference(quantumId: String): SnoozePreferenceDto {
        val notificationSnoozePreference = repository.findByQuantumIdAndSnoozeGreaterThanEqual(quantumId, date)
        return SnoozePreferenceDto.from(notificationSnoozePreference.orElse(SnoozePreference(quantumId, null)))
    }
}