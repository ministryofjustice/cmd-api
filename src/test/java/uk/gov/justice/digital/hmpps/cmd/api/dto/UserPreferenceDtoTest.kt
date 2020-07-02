package uk.gov.justice.digital.hmpps.cmd.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.model.UserPreference
import java.time.LocalDate

class UserPreferenceDtoTest {

    @Test
    fun `Create User Preference Dto`() {
        val quantumId = "XYZ"
        val date = LocalDate.now()
        val userPreference = UserPreference(quantumId, date)

        val userPreferenceDto = UserPreferenceDto.from(userPreference)

        Assertions.assertThat(userPreferenceDto.snoozeUntil).isEqualTo(date)

    }
}