package uk.gov.justice.digital.hmpps.cmd.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference
import uk.gov.justice.digital.hmpps.cmd.api.model.UserPreference
import java.time.LocalDate

@Schema(description = "User Preference")
data class UserPreferenceDto @JsonCreator constructor(
  @Schema(required = false, description = "Date to snooze notifications until", example = "2020-08-27")
  @JsonProperty("snoozeUntil")
  val snoozeUntil: LocalDate?,
  @Schema(required = false, description = "User's Email Address", example = "me@example.com")
  @JsonProperty("email")
  val email: String?,
  @Schema(required = false, description = "User's SMS number", example = "0123567890")
  @JsonProperty("sms")
  val sms: String?,
  @Schema(required = false, description = "User's preferred method of communication", example = "EMAIL")
  @JsonProperty("preference")
  val preference: CommunicationPreference?,
) {

  companion object {
    fun from(userPreference: UserPreference?): UserPreferenceDto? =
      userPreference?.let {
        UserPreferenceDto(
          it.snoozeUntil,
          it.email,
          it.sms,
          it.commPref,
        )
      }
  }
}
