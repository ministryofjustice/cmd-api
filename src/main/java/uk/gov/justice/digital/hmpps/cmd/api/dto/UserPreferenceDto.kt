package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.UserPreference
import java.time.LocalDate

@ApiModel(description = "User Preference")
data class UserPreferenceDto @JsonCreator constructor(
        @ApiModelProperty(required = true, value = "Date to snooze notifications until", position = 1, example = "2020-08-27")
        @JsonProperty("snoozeUntil")
        val snoozeUntil: LocalDate?) {

    companion object {
        fun from(userPreference: UserPreference): UserPreferenceDto {
            return UserPreferenceDto(userPreference.snoozeUntil)
        }
    }
}
