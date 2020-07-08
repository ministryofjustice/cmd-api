package uk.gov.justice.digital.hmpps.cmd.api.model

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ShiftTaskNotificationTest {

    @Test
    fun `Should return a valid shift task notification`() {
        val date = LocalDateTime.now()

        val quantumId = "XYZ"
        val description = "Any Description,"
        val taskDate = date.plusDays(2)
        val taskStartSec = 123
        val taskEndSec = 456
        val activity = "Any Activity"
        val lastModified = date.plusDays(3)
        val acknowledged = false

        val shiftTaskNotification = ShiftTaskNotification(
                quantumId,
                description,
                taskDate,
                taskStartSec,
                taskEndSec,
                activity,
                lastModified,
                acknowledged
        )

        Assertions.assertThat(shiftTaskNotification.quantumId).isEqualTo(quantumId)
        Assertions.assertThat(shiftTaskNotification.description).isEqualTo(description)
        Assertions.assertThat(shiftTaskNotification.taskDate).isEqualTo(taskDate)
        Assertions.assertThat(shiftTaskNotification.taskStartTimeInSeconds).isEqualTo(taskStartSec)
        Assertions.assertThat(shiftTaskNotification.taskEndTimeInSeconds).isEqualTo(taskEndSec)
        Assertions.assertThat(shiftTaskNotification.activity).isEqualTo(activity)
        Assertions.assertThat(shiftTaskNotification.lastModifiedDateTime).isEqualTo(lastModified)
        Assertions.assertThat(shiftTaskNotification.acknowledged).isEqualTo(acknowledged)

    }
}