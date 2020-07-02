package uk.gov.justice.digital.hmpps.cmd.api.model

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ShiftNotificationTest {

    @Test
    fun `Should return a valid shift notification`() {
        val date = LocalDateTime.now()

        val quantumId = "XYZ"
        val dateTime = date.plusDays(1)
        val description = "Any Description,"
        val shiftDate = date.plusDays(2)
        val lastModified = date.plusDays(3)
        val read = false
        val sentSms = true
        val sentEmail = false
        val lastModSec = 1234L

        val shiftNotification = ShiftNotification(
                quantumId,
                dateTime,
                description,
                shiftDate,
                lastModified,
                read,
                sentSms,
                sentEmail,
                lastModSec
        )

        Assertions.assertThat(shiftNotification.quantumId).isEqualTo(quantumId)
        Assertions.assertThat(shiftNotification.dateTime).isEqualTo(dateTime)
        Assertions.assertThat(shiftNotification.description).isEqualTo(description)
        Assertions.assertThat(shiftNotification.shiftDate).isEqualTo(shiftDate)
        Assertions.assertThat(shiftNotification.lastModifiedDateTime).isEqualTo(lastModified)
        Assertions.assertThat(shiftNotification.read).isEqualTo(read)
        Assertions.assertThat(shiftNotification.sentSms).isEqualTo(sentSms)
        Assertions.assertThat(shiftNotification.sentEmail).isEqualTo(sentEmail)
        Assertions.assertThat(shiftNotification.lastModifiedDateTimeInSeconds).isEqualTo(lastModSec)

    }
}