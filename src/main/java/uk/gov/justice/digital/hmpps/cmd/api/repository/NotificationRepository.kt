package uk.gov.justice.digital.hmpps.cmd.api.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailModificationType
import uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.cmd.api.model.Notification
import java.time.LocalDateTime

@Repository
interface NotificationRepository : CrudRepository<Notification, Long> {

  fun findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId: String, from: LocalDateTime, to: LocalDateTime): Collection<Notification>

  fun findAllByProcessedIsFalse(): List<Notification>

  fun countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
    quantumId: String,
    detailStart: LocalDateTime,
    shiftType: ShiftType,
    shiftModified: LocalDateTime,
  ): Int

  fun countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndActionType(
    quantumId: String,
    detailStart: LocalDateTime,
    shiftType: ShiftType,
    actionType: DetailModificationType,
  ): Int

  // Workaround for https://github.com/spring-projects/spring-data-jpa/issues/4018 by returning java.lang.Integer
  fun deleteAllByShiftModifiedBefore(shiftModified: LocalDateTime): Integer
}
