package uk.gov.justice.digital.hmpps.cmd.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailDisplayType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.DetailParentType
import java.time.LocalDateTime

@ApiModel(description = "Shift")
data class DetailDto @JsonCreator constructor(
        @ApiModelProperty(value = "The human readable activity", example = "Internal Training")
        @JsonProperty("activity")
        val activity: String?,

        @ApiModelProperty(value = "The start of the detail", example = "2020-04-20T17:00:00")
        @JsonProperty("start")
        val start: LocalDateTime,

        @ApiModelProperty(value = "The end of the detail", example = "2020-04-20T17:00:00")
        @JsonProperty("end")
        val end: LocalDateTime,

        @ApiModelProperty(value = "The type of the Shift the detail belongs to", example = "OVERTIME")
        @JsonProperty("parentType")
        val detail: DetailParentType,

        @ApiModelProperty(value = "Hint for the UI on how to display this entity", example = "NIGHT_FINISH")
        @JsonProperty("displayType")
        var displayType: DetailDisplayType? = null,

        @ApiModelProperty(value = "If there is a display type", example = "2020-04-20T17:00:00")
        @JsonProperty("displayTypeTime")
        var displayTypeTime: LocalDateTime? = null,

        @ApiModelProperty(value = "If the type is a 'finish' we include the duration of the whole shift in seconds", example = "54321")
        @JsonProperty("finishDuration")
        var finishDuration: Long? = null

)