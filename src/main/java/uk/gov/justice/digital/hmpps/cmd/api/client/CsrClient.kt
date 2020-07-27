package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.client

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.client.dto.ShiftNotificationDto

// Blank client to help with writing the service
@Component
class CsrClient {

    fun getShiftNotifications(planUnit: String, region: Int): Collection<ShiftNotificationDto> {
        return listOf()
    }

    fun getShiftTaskNotifications(): Collection<ShiftNotificationDto> {
        return listOf()
    }
}