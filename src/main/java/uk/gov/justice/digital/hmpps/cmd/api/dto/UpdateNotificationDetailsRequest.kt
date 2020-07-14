package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference

@ApiModel(description = "Update notification details for a user")
data class UpdateNotificationDetailsRequest(
        @ApiModelProperty(required = true, value = "Users email address", position = 1, example = "user@example.com")
        @JsonProperty("email")
        val email: String,

        @ApiModelProperty(required = true, value = "Users sms number", position = 2, example = "07123456789")
        @JsonProperty("sms")
        val sms: String,

        @ApiModelProperty(required = true, value = "Preferred method of contact", position = 3, example = "EMAIL")
        @JsonProperty("preference")
        val commPref: CommunicationPreference)
