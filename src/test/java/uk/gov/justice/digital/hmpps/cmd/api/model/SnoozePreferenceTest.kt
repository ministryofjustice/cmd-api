package uk.gov.justice.digital.hmpps.cmd.api.model

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.SnoozePreference
import java.time.LocalDate

class SnoozePreferenceTest {

    @Test
    fun `Should return a valid date`() {
        val quantumId = "XYZ"
        val date = LocalDate.now()
        val snoozePref = SnoozePreference(quantumId, date)

        Assertions.assertThat(snoozePref.quantumId).isEqualTo(quantumId)
        Assertions.assertThat(snoozePref.snoozeUntil).isEqualTo(date)
    }
}