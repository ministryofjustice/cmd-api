package uk.gov.justice.digital.hmpps.cmd.api.model

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate

class UserPreferenceTest {

    @Test
    fun `Should return a valid snooze until date`() {
        val quantumId = "XYZ"
        val date = LocalDate.now()
        val email = "email"
        val sms = "sms"
        val preference = "EMAIL"
        val userPref = UserPreference(quantumId, date, email, sms, preference)

        Assertions.assertThat(userPref.quantumId).isEqualTo(quantumId)
        Assertions.assertThat(userPref.snoozeUntil).isEqualTo(date)
        Assertions.assertThat(userPref.email).isEqualTo(email)
        Assertions.assertThat(userPref.sms).isEqualTo(sms)
        Assertions.assertThat(userPref.commPref).isEqualTo(preference)
    }
}