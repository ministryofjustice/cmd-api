package uk.gov.justice.digital.hmpps.cmd.api.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftActionType
import uk.gov.justice.digital.hmpps.cmd.api.model.Notification
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import java.time.LocalDateTime

@Repository
interface NotificationRepository : CrudRepository<Notification, Long> {

    fun findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId: String, from: LocalDateTime, to: LocalDateTime): Collection<Notification>

    fun findAllByProcessedIsFalse(): Collection<Notification>

    fun countAllByQuantumIdIgnoreCaseAndDetailStartAndShiftTypeAndShiftModified(quantumId: String, detailStart: LocalDateTime, shiftType: ShiftType, shiftModified: LocalDateTime): Int

    fun countAllByQuantumIdIgnoreCaseAndDetailStartAndShiftTypeAndActionType(quantumId: String, detailStart: LocalDateTime, shiftType: ShiftType, actionType: ShiftActionType): Int

}
