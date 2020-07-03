package uk.gov.justice.digital.hmpps.cmd.api.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.cmd.api.model.ShiftTaskNotification
import java.time.LocalDateTime

@Repository
interface ShiftTaskNotificationRepository : CrudRepository<ShiftTaskNotification, Long> {

   fun findAllByQuantumIdAndLastModifiedDateTimeIsBetween(quantumId: String, from: LocalDateTime, to: LocalDateTime): Collection<ShiftTaskNotification>

}
