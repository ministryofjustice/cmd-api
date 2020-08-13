package uk.gov.justice.digital.hmpps.cmd.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalTime

@ApiModel(description = "Shift")
data class DayEventDto @JsonCreator constructor(

        @ApiModelProperty(value = "The time the even occours at", position = 1, example = "07:40")
        @JsonProperty("eventTime")
        val eventTime : LocalTime,

        @ApiModelProperty(value = "Hint for the UI on how to display this task", position = 2, example = "night_finish")
        @JsonProperty("displayType")
        var displayType : String,

        @ApiModelProperty(value = "If the type is a 'finish' we include put the shift duration.", position = 3, example = "3h 24m")
        @JsonProperty("finishDuration")
        var finishDuration : String? = null

)



