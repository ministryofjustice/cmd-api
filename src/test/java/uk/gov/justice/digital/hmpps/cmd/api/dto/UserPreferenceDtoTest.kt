package uk.gov.justice.digital.hmpps.cmd.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto.UserPreferenceDto
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.SnoozePreference
import java.time.LocalDate

class UserPreferenceDtoTest {

    @Test
    fun `Create Snooze Preference Dto`() {
        val quantumId = "XYZ"
        val date = LocalDate.now()
        val snoozePreference = SnoozePreference(quantumId, date)

        val snoozePreferenceDto = UserPreferenceDto.from(snoozePreference)

        Assertions.assertThat(snoozePreferenceDto.snoozeUntil).isEqualTo(date)

    }
}