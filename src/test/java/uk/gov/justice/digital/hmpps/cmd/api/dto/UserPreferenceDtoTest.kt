package uk.gov.justice.digital.hmpps.cmd.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference
import uk.gov.justice.digital.hmpps.cmd.api.model.UserPreference
import java.time.LocalDate

class UserPreferenceDtoTest {

  @Test
  fun `Create User Preference Dto`() {
    val quantumId = "XYZ"
    val date = LocalDate.now()
    val email = "email"
    val sms = "sms"
    val pref = CommunicationPreference.EMAIL
    val userPreference = UserPreference(quantumId, date, email, sms, pref)

    val userPreferenceDto = UserPreferenceDto.from(userPreference)

    Assertions.assertThat(userPreferenceDto?.snoozeUntil).isEqualTo(date)
    Assertions.assertThat(userPreferenceDto?.email).isEqualTo(email)
    Assertions.assertThat(userPreferenceDto?.sms).isEqualTo(sms)
    Assertions.assertThat(userPreferenceDto?.preference).isEqualTo(pref)
  }
}
