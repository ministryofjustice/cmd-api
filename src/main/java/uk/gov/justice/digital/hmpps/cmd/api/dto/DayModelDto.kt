package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalDate
import java.time.LocalDateTime

@ApiModel(description = "Day model")
data class DayModelDto @JsonCreator constructor(
        @ApiModelProperty(value = "The date of the shift", position = 1, example = "2020-04-20")
        @JsonProperty("date")
        val date : LocalDate,
        @ApiModelProperty(value = "The value for the Start box on the given date", position = 2, example = "2020-04-20T17:00:00")
        @JsonProperty("dailyStartDateTime")
        val dailyStartDateTime : LocalDateTime?,
        @ApiModelProperty(value = "The value for the Finish box on the given date", position = 2, example = "2020-04-20T18:45:00")
        @JsonProperty("dailyEndDateTime")
        val dailyEndDateTime : LocalDateTime?,
        @ApiModelProperty(value = "The type of Shift", position = 3, example = "Rest Day")
        @JsonProperty("type")
        val type : String,
        @ApiModelProperty(value = "The start of the shift", position = 4, example = "2020-04-20T17:00:00")
        @JsonProperty("startDateTime")
        val startDateTime : LocalDateTime,
        @ApiModelProperty(value = "The end of the shift", position = 5, example = "2020-04-20T18:45:00")
        @JsonProperty("endDateTime")
        val endDateTime : LocalDateTime,
        @ApiModelProperty(value = "The duration of the shift in seconds, excluding breaks", position = 6, example = "1234567")
        @JsonProperty("durationInSeconds")
        val durationInSeconds : Long,
        @ApiModelProperty(value = "A collection of tasks that make up the shift", position = 7)
        @JsonProperty("tasks")
        val tasks : Collection<TaskModelDto>
)



