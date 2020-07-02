package uk.gov.justice.digital.hmpps.cmd.api.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.cmd.api.model.ShiftNotification
import java.time.LocalDateTime
import java.util.*

@Repository
interface ShiftNotificationRepository : CrudRepository<ShiftNotification, UUID> {

   fun findAllByQuantumIdAndLastModifiedDateTimeIsBetween(quantumId: String, from: LocalDateTime, to: LocalDateTime): Collection<ShiftNotification>

}
