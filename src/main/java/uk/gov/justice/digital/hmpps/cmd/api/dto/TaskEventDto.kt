package uk.gov.justice.digital.hmpps.cmd.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalTime

@ApiModel(description = "Shift")
data class TaskEventDto @JsonCreator constructor(
        @ApiModelProperty(value = "The readable label for the task", position = 1, example = "Internal Training")
        @JsonProperty("label")
        val label : String,

        @ApiModelProperty(value = "The start of the task", position = 3, example = "2020-04-20T17:00:00")
        @JsonProperty("startTime")
        val start : LocalTime?,

        @ApiModelProperty(value = "The start of the task", position = 4, example = "2020-04-20T17:00:00")
        @JsonProperty("endTime")
        val end : LocalTime?,

        @ApiModelProperty(value = "Hint for the UI on how to display this entity", position = 5, example = "NIGHT_FINISH")
        @JsonProperty("displayType")
        var displayType : String?,

        @ApiModelProperty(value = "If the type is a 'finish' we include put the shift duration.", position = 6, example = "3h 24m")
        @JsonProperty("finishDuration")
        var finishDuration : String? = null

)


