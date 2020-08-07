package uk.gov.justice.digital.hmpps.cmd.api.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.cmd.api.model.ShiftNotification
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftNotificationType
import java.time.LocalDateTime

@Repository
interface ShiftNotificationRepository : CrudRepository<ShiftNotification, Long> {

    fun findAllByQuantumIdAndShiftModifiedIsBetween(quantumId: String, from: LocalDateTime, to: LocalDateTime): Collection<ShiftNotification>

    fun findAllByProcessedIsFalse(): Collection<ShiftNotification>

    fun countAllByQuantumIdAndShiftDateAndShiftTypeAndShiftModified(quantumId: String, shiftDateTime: LocalDateTime, shiftNotificationType: String, shiftModified: LocalDateTime): Int

}
