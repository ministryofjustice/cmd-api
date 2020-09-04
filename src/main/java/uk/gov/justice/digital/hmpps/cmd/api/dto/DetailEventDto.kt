package uk.gov.justice.digital.hmpps.cmd.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.cmd.api.domain.TaskDisplayType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import java.time.LocalDateTime

@ApiModel(description = "Shift")
data class DetailEventDto @JsonCreator constructor(
        @ApiModelProperty(value = "The readable label for the task", position = 1, example = "Internal Training")
        @JsonProperty("label")
        val label : String?,

        @ApiModelProperty(value = "The start of the task", position = 3, example = "2020-04-20T17:00:00")
        @JsonProperty("startTime")
        val start : LocalDateTime,

        @ApiModelProperty(value = "The start of the task", position = 4, example = "2020-04-20T17:00:00")
        @JsonProperty("endTime")
        val end : LocalDateTime,

        @ApiModelProperty(value = "The type of Detail", position = 4, example = "OVERTIME")
        @JsonProperty("entityType")
        val shiftType : ShiftType,

        @ApiModelProperty(value = "Hint for the UI on how to display this entity", position = 5, example = "NIGHT_FINISH")
        @JsonProperty("displayType")
        var displayType : TaskDisplayType? = null,

        @ApiModelProperty(value = "Hint for the UI on how to display this entity", position = 5, example = "2020-04-20T17:00:00")
        @JsonProperty("displayTime")
        var eventDateTime : LocalDateTime? = null,

        @ApiModelProperty(value = "If the type is a 'finish' we include put the shift duration.", position = 6, example = "3h 24m")
        @JsonProperty("finishDuration")
        var finishDuration : String? = null

)