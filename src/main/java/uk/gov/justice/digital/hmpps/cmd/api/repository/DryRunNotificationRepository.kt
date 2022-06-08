package uk.gov.justice.digital.hmpps.cmd.api.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailModificationType
import uk.gov.justice.digital.hmpps.cmd.api.model.DryRunNotification
import uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import java.time.LocalDateTime

@Repository
interface DryRunNotificationRepository : CrudRepository<DryRunNotification, Long> {

  fun findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId: String, from: LocalDateTime, to: LocalDateTime): Collection<DryRunNotification>

  fun findAllByProcessedIsFalse(): List<DryRunNotification>

  fun countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
    quantumId: String,
    detailStart: LocalDateTime,
    shiftType: ShiftType,
    shiftModified: LocalDateTime
  ): Int

  fun countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndActionType(
    quantumId: String,
    detailStart: LocalDateTime,
    shiftType: ShiftType,
    actionType: DetailModificationType
  ): Int

  fun deleteAllByShiftModifiedBefore(shiftModified: LocalDateTime): Int
}
