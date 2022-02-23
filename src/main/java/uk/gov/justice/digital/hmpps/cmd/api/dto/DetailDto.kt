package uk.gov.justice.digital.hmpps.cmd.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.cmd.api.domain.TaskDisplayType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import java.time.LocalDateTime

@Schema(description = "Shift")
data class DetailDto @JsonCreator constructor(
  @Schema(description = "The human readable activity", example = "Internal Training")
  @JsonProperty("activity")
  val activity: String?,

  @Schema(description = "The start of the detail", example = "2020-04-20T17:00:00")
  @JsonProperty("start")
  val start: LocalDateTime,

  @Schema(description = "The end of the detail", example = "2020-04-20T17:00:00")
  @JsonProperty("end")
  val end: LocalDateTime,

  @Schema(description = "The type of the Shift the detail belongs to", example = "OVERTIME")
  @JsonProperty("parentType")
  val detail: ShiftType,

  @Schema(description = "Hint for the UI on how to display this entity", example = "NIGHT_FINISH")
  @JsonProperty("displayType")
  var displayType: TaskDisplayType? = null,

  @Schema(description = "If there is a display type", example = "2020-04-20T17:00:00")
  @JsonProperty("displayTypeTime")
  var displayTypeTime: LocalDateTime? = null,

  @Schema(description = "If the type is a 'finish' we include the duration of the whole shift in seconds", example = "54321")
  @JsonProperty("finishDuration")
  var finishDuration: Long? = null

)
