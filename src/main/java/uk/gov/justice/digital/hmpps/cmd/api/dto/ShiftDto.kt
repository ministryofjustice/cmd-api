package uk.gov.justice.digital.hmpps.cmd.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.FullDayActivityType
import java.time.LocalDate

@Schema(description = "Shift")
data class ShiftDto @JsonCreator constructor(
  @Schema(description = "The date of the shift", example = "2020-04-20")
  @JsonProperty("date")
  val date: LocalDate,

  @Schema(description = "The type of the full day", example = "ABSENCE")
  @JsonProperty("fullDayType")
  val shiftType: FullDayActivityType,

  @Schema(description = "The human readable type of the full day", example = "Absence")
  @JsonProperty("fullDayTypeDescription")
  val shiftTypeDescription: String,

  @Schema(description = "A collection of details that make up the shift")
  @JsonProperty("details")
  val details: Collection<DetailDto>
)
