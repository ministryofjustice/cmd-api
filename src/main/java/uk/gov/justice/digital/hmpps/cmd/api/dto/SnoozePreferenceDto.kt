package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.SnoozePreference
import java.time.LocalDate


data class SnoozePreferenceDto @JsonCreator constructor(
        @JsonProperty("snoozeDate")
        val snoozeDate: LocalDate?) {

    companion object {
        fun from(snoozePreference: SnoozePreference): SnoozePreferenceDto {
            return SnoozePreferenceDto(snoozePreference.snooze)
        }
    }
}
