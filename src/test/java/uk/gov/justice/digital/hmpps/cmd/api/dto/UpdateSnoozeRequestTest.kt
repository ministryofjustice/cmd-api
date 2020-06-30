package uk.gov.justice.digital.hmpps.cmd.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto.UpdateSnoozeRequest
import java.time.LocalDate

class UpdateSnoozeRequestTest {

    @Test
    fun `Create Update Snooze Request`() {
        val date = LocalDate.now()
        val updateSnoozeRequest = UpdateSnoozeRequest(date)

        Assertions.assertThat(updateSnoozeRequest.snooze).isEqualTo(date)
    }
}