package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.SnoozePreference
import java.time.LocalDate

@ApiModel(description = "Preference")
data class PreferenceDto @JsonCreator constructor(
        @ApiModelProperty(required = true, value = "Date to snooze until", position = 1, example = "2020-08-27")
        @JsonProperty("snooze")
        val snooze: LocalDate?) {

    companion object {
        fun from(snoozePreference: SnoozePreference): PreferenceDto {
            return PreferenceDto(snoozePreference.snooze)
        }
    }
}
