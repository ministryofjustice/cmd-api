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

        @ApiModelProperty(value = "The type of the full day", position = 2, example = "Absent")
        @JsonProperty("fullDayType")
        val fullDayType : String,

        @ApiModelProperty(value = "The display text for the full day", position = 3, example = "Training")
        @JsonProperty("fullDayDescription")
        val fullDayDescription : String,

        @ApiModelProperty(value = "A collection of tasks that make up the shift", position = 4)
        @JsonProperty("tasks")
        val tasks : Collection<DayEventDto>
)



