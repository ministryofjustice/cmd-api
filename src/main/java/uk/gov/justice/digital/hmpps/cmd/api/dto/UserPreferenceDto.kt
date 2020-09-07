package uk.gov.justice.digital.hmpps.cmd.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference
import uk.gov.justice.digital.hmpps.cmd.api.model.UserPreference
import java.time.LocalDate

@ApiModel(description = "User Preference")
data class UserPreferenceDto @JsonCreator constructor(
        @ApiModelProperty(required = false, value = "Date to snooze notifications until", position = 1, example = "2020-08-27")
        @JsonProperty("snoozeUntil")
        val snoozeUntil: LocalDate?,
        @ApiModelProperty(required = false, value = "User's Email Address", position = 1, example = "me@example.com")
        @JsonProperty("email")
        val email: String?,
        @ApiModelProperty(required = false, value = "User's SMS number", position = 1, example = "0123567890")
        @JsonProperty("sms")
        val sms: String?,
        @ApiModelProperty(required = false, value = "User's preferred method of communication", position = 1, example = "EMAIL")
        @JsonProperty("preference")
        val preference: CommunicationPreference?) {

    companion object {
        fun from(userPreference: UserPreference?): UserPreferenceDto {
            return UserPreferenceDto(userPreference?.snoozeUntil, userPreference?.email, userPreference?.sms, userPreference?.commPref)
        }
    }
}
