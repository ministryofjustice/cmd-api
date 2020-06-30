package uk.gov.justice.digital.hmpps.cmd.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto.UpdateSnoozeUntilRequest
import java.time.LocalDate

class UpdateSnoozeUntilRequestTest {

    @Test
    fun `Create Update Snooze Request`() {
        val date = LocalDate.now()
        val updateSnoozeRequest = UpdateSnoozeUntilRequest(date)

        Assertions.assertThat(updateSnoozeRequest.snoozeUntil).isEqualTo(date)
    }
}