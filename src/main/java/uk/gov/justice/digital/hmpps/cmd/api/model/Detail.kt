package uk.gov.justice.digital.hmpps.cmd.api.model

import java.time.LocalDate
import java.time.LocalDateTime

data class Detail(
  val quantumId: String?,
  val shiftModified: LocalDateTime?,
  var shiftDate: LocalDate,
  val shiftType: Int,
  val startTimeInSeconds: Long?,
  val endTimeInSeconds: Long?,
  val activity: String?,
  val actionType: Int?,
  val templateName: String?,
)
