package uk.gov.justice.digital.hmpps.cmd.api.model

import java.time.LocalDate
import java.time.LocalDateTime

data class CmdNotification(
  val id: Long,
  val levelId: Int,
  val onDate: LocalDate,
  val quantumId: String?,
  val lastModified: LocalDateTime?,
  val actionType: Int?,
  val startTimeInSeconds: Long?,
  val endTimeInSeconds: Long?,
  val activity: String?,
)
