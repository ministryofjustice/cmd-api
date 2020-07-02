package uk.gov.justice.digital.hmpps.cmd.api.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.cmd.api.model.ShiftTaskNotification
import java.time.LocalDateTime
import java.util.*

@Repository
interface ShiftTaskNotificationRepository : CrudRepository<ShiftTaskNotification, UUID> {

   fun findAllByQuantumIdAndLastModifiedDateTimeIsBetween(quantumId: String, from: LocalDateTime, to: LocalDateTime): Collection<ShiftTaskNotification>

}
