package uk.gov.justice.digital.hmpps.cmd.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "Update 'notification snooze until' date request")
data class UpdateSnoozeUntilRequest(
  @Schema(required = true, description = "Date to snooze notifications until", example = "2020-08-27")
  @JsonProperty("snoozeUntil")
  val snoozeUntil: LocalDate
)
