package uk.gov.justice.digital.hmpps.cmd.api.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.cmd.api.model.ShiftNotification
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
interface ShiftNotificationRepository : CrudRepository<ShiftNotification, Long> {

    fun findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId: String, from: LocalDateTime, to: LocalDateTime): Collection<ShiftNotification>

    fun findAllByProcessedIsFalse(): Collection<ShiftNotification>

    fun countAllByQuantumIdIgnoreCaseAndShiftDateAndShiftTypeIgnoreCaseAndShiftModified(quantumId: String, shiftDate: LocalDate, shiftNotificationType: String, shiftModified: LocalDateTime): Int

    fun countAllByQuantumIdIgnoreCaseAndShiftDateAndShiftTypeIgnoreCaseAndActionTypeIgnoreCase(quantumId: String, shiftDate: LocalDate, shiftNotificationType: String, actionType: String): Int

}
