package uk.gov.justice.digital.hmpps.cmd.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalDate

@ApiModel(description = "Update 'notification snooze until' date request")
data class UpdateSnoozeUntilRequest(
  @ApiModelProperty(required = true, value = "Date to snooze notifications until", example = "2020-08-27")
  @JsonProperty("snoozeUntil")
  val snoozeUntil: LocalDate
)
