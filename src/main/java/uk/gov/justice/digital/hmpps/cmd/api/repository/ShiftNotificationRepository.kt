package uk.gov.justice.digital.hmpps.cmd.api.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.cmd.api.model.ShiftNotification
import java.time.LocalDateTime

@Repository
interface ShiftNotificationRepository : CrudRepository<ShiftNotification, Long> {

    fun findAllByQuantumIdAndShiftModifiedIsBetween(quantumId: String, from: LocalDateTime, to: LocalDateTime): Collection<ShiftNotification>

    fun findAllByProcessedIsFalse(): Collection<ShiftNotification>

    fun countAllByQuantumIdAndShiftDateAndShiftTypeAndShiftModified(quantumId: String, shiftDate: LocalDate, shiftNotificationType: String, shiftModified: LocalDateTime): Int

    fun countAllByQuantumIdAndShiftDateAndShiftTypeAndShiftModifiedAndActionType(quantumId: String, shiftDate: LocalDate, shiftNotificationType: String, shiftModified: LocalDateTime, actionType: String): Int

}
