package uk.gov.justice.digital.hmpps.cmd.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalDate

@ApiModel(description = "Shift")
data class ShiftDto @JsonCreator constructor(
        @ApiModelProperty(value = "The date of the shift", example = "2020-04-20")
        @JsonProperty("date")
        val date : LocalDate,

        @ApiModelProperty(value = "The type of the full day", example = "Absent")
        @JsonProperty("fullDayType")
        val shiftType : String,

        @ApiModelProperty(value = "A collection of details that make up the shift")
        @JsonProperty("details")
        val details : Collection<DetailDto>
)