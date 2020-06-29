package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class UpdateSnoozeRequest(
        @JsonProperty("snooze")
        val snooze: LocalDate)
