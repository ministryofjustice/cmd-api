package uk.gov.justice.digital.hmpps.cmd.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern
import uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference

@Schema(description = "Update notification details for a user")
data class UpdateNotificationDetailsRequest(
  @Schema(required = true, description = "Users email address", example = "user@example.com")
  @JsonProperty("email")
  @get: Email(message = "Not a valid Email address.")
  val email: String,

  @Schema(required = false, description = "Users sms number", example = "07123456789")
  @JsonProperty("sms")
  @get: Pattern(regexp = "((07)[0-9]{8,9})|^\$", message = "Not a valid mobile number")
  val sms: String?,

  @Schema(required = true, description = "Preferred method of contact", example = "EMAIL")
  @JsonProperty("preference")
  val commPref: CommunicationPreference,
)
