package uk.gov.justice.digital.hmpps.cmd.api.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailModificationType
import uk.gov.justice.digital.hmpps.cmd.api.model.Notification
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.DetailParentType
import java.time.LocalDateTime

@Repository
interface NotificationRepository : CrudRepository<Notification, Long> {

    fun findAllByQuantumIdAndShiftModifiedIsBetween(quantumId: String, from: LocalDateTime, to: LocalDateTime): Collection<Notification>

    fun findAllByProcessedIsFalse(): Collection<Notification>

    fun countAllByQuantumIdAndDetailStartAndParentTypeAndShiftModified(quantumId: String, detailStart: LocalDateTime, shiftType: DetailParentType, shiftModified: LocalDateTime): Int

    fun countAllByQuantumIdAndDetailStartAndParentTypeAndActionType(quantumId: String, detailStart: LocalDateTime, shiftType: DetailParentType, actionType: DetailModificationType): Int

    fun deleteAllByShiftModifiedBefore(shiftModified: LocalDateTime)
}
