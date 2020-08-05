package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalDate
import java.time.LocalDateTime

@ApiModel(description = "Shift")
data class TaskModelDto @JsonCreator constructor(
        @ApiModelProperty(value = "The date of the shift", position = 1, example = "2020-04-20")
        @JsonProperty("date")
        val date : LocalDate,
        @ApiModelProperty(value = "The value for the Start box on the given date", position = 2, example = "2020-04-20T17:00:00")
        @JsonProperty("dailyStartDateTime")
        val dailyStartDateTime : LocalDateTime?,
        @ApiModelProperty(value = "The value for the Finish box on the given date", position = 2, example = "2020-04-20T18:45:00")
        @JsonProperty("dailyEndDateTime")
        val dailyEndDateTime : LocalDateTime?,
        @ApiModelProperty(value = "The readable lable for the activity", position = 3, example = "Internal Training")
        @JsonProperty("label")
        val label : String,
        @ApiModelProperty(value = "The type of task", position = 3, example = "UNSPECIFIC")
        @JsonProperty("taskType")
        val type : String,
        @ApiModelProperty(value = "The start of the shift", position = 4, example = "2020-04-20T17:00:00")
        @JsonProperty("startDateTime")
        val startDateTime : LocalDateTime,
        @ApiModelProperty(value = "The end of the shift", position = 5, example = "2020-04-20T18:45:00")
        @JsonProperty("endDateTime")
        val endDateTime : LocalDateTime
)



