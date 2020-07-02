package uk.gov.justice.digital.hmpps.cmd.api.model

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ShiftTaskNotificationTest {

    @Test
    fun `Should return a valid shift task notification`() {
        val date = LocalDateTime.now()

        val quantumId = "XYZ"
        val dateTime = date.plusDays(1)
        val description = "Any Description,"
        val taskDate = date.plusDays(2)
        val taskStartSec = 123
        val taskEndSec = 456
        val activity = "Any Activity"
        val lastModified = date.plusDays(3)
        val read = false
        val sentSms = true
        val sentEmail = false
        val lastModSec = 1234L

        val shiftTaskNotification = ShiftTaskNotification(
                quantumId,
                dateTime,
                description,
                taskDate,
                taskStartSec,
                taskEndSec,
                activity,
                lastModified,
                read,
                sentSms,
                sentEmail,
                lastModSec
        )

        Assertions.assertThat(shiftTaskNotification.quantumId).isEqualTo(quantumId)
        Assertions.assertThat(shiftTaskNotification.dateTime).isEqualTo(dateTime)
        Assertions.assertThat(shiftTaskNotification.description).isEqualTo(description)
        Assertions.assertThat(shiftTaskNotification.taskDate).isEqualTo(taskDate)
        Assertions.assertThat(shiftTaskNotification.taskStartTimeInSeconds).isEqualTo(taskStartSec)
        Assertions.assertThat(shiftTaskNotification.taskEndTimeInSeconds).isEqualTo(taskEndSec)
        Assertions.assertThat(shiftTaskNotification.activity).isEqualTo(activity)
        Assertions.assertThat(shiftTaskNotification.lastModifiedDateTime).isEqualTo(lastModified)
        Assertions.assertThat(shiftTaskNotification.read).isEqualTo(read)
        Assertions.assertThat(shiftTaskNotification.sentSms).isEqualTo(sentSms)
        Assertions.assertThat(shiftTaskNotification.sentEmail).isEqualTo(sentEmail)
        Assertions.assertThat(shiftTaskNotification.lastModifiedDateTimeInSeconds).isEqualTo(lastModSec)

    }
}