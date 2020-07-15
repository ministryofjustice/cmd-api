package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference
import javax.validation.constraints.Email
import javax.validation.constraints.Pattern

@ApiModel(description = "Update notification details for a user")
data class UpdateNotificationDetailsRequest(
        @ApiModelProperty(required = true, value = "Users email address", position = 1, example = "user@example.com")
        @JsonProperty("email")
        @get: Email(message = "Not a valid Email address.")
        val email: String,

        @ApiModelProperty(required = true, value = "Users sms number", position = 2, example = "07123456789")
        @JsonProperty("sms")
        @get: Pattern(regexp = "((07)[0-9]{8,9})|^\$", message = "Not a valid mobile number")
        val sms: String,

        @ApiModelProperty(required = true, value = "Preferred method of contact", position = 3, example = "EMAIL")
        @JsonProperty("preference")
        val commPref: CommunicationPreference)
