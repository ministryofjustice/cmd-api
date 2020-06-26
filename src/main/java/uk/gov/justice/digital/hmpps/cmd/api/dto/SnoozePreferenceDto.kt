package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.SnoozePreference
import java.time.LocalDate
import javax.validation.constraints.NotBlank


data class SnoozePreferenceDto @JsonCreator constructor(
        @JsonProperty("snoozeDate")
        val snoozeDate: @NotBlank LocalDate) {

    companion object {
        fun from(snoozePreference: SnoozePreference): SnoozePreferenceDto {
            return SnoozePreferenceDto(snoozePreference.snooze)
        }
    }
}
