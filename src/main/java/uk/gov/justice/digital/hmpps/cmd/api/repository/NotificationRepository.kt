package uk.gov.justice.digital.hmpps.cmd.api.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailModificationType
import uk.gov.justice.digital.hmpps.cmd.api.model.Notification
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.DetailParentType
import java.time.LocalDateTime

@Repository
interface NotificationRepository : CrudRepository<Notification, Long> {

    fun findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId: String, from: LocalDateTime, to: LocalDateTime): Collection<Notification>

    fun findAllByProcessedIsFalse(): Collection<Notification>

    fun countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(quantumId: String, detailStart: LocalDateTime, shiftType: DetailParentType, shiftModified: LocalDateTime): Int

    fun countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndActionType(quantumId: String, detailStart: LocalDateTime, shiftType: DetailParentType, actionType: DetailModificationType): Int

}
