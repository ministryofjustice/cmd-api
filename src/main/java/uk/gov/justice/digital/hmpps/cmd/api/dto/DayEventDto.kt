package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalDate
import java.time.LocalDateTime

@ApiModel(description = "Shift")
data class DayEventDto @JsonCreator constructor(
        @ApiModelProperty(value = "The date of the shift", position = 1, example = "2020-04-20")
        @JsonProperty("date")
        val date : LocalDate,

        @ApiModelProperty(value = "The readable label for the activity", position = 3, example = "Internal Training")
        @JsonProperty("label")
        val label : String,

        @ApiModelProperty(value = "The type of task", position = 3, example = "Shift")
        @JsonProperty("taskType")
        val taskType : String,

        @ApiModelProperty(value = "The start of the shift", position = 4, example = "2020-04-20T17:00:00")
        @JsonProperty("startDateTime")
        val time : LocalDateTime,

        @ApiModelProperty(value = "Hint for the UI on how to display this entity", position = 5, example = "NIGHT_FINISH")
        @JsonProperty("displayType")
        val displayType : String,

        @ApiModelProperty(value = "If the type is a 'finish' we include put the shift duration.", position = 6, example = "3h 24m")
        @JsonProperty("finishDuration")
        var finishDuration : String? = null

)



