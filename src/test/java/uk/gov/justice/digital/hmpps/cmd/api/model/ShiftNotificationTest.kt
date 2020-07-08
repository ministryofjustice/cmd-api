package uk.gov.justice.digital.hmpps.cmd.api.model

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ShiftNotificationTest {

    @Test
    fun `Should return a valid shift notification`() {
        val date = LocalDateTime.now()

        val quantumId = "XYZ"
        val description = "Any Description,"
        val shiftDate = date.plusDays(2)
        val lastModified = date.plusDays(3)
        val notificationType = 0L
        val acknowledged = false

        val shiftNotification = ShiftNotification(
                quantumId,
                description,
                shiftDate,
                lastModified,
                notificationType,
                acknowledged
        )

        Assertions.assertThat(shiftNotification.quantumId).isEqualTo(quantumId)
        Assertions.assertThat(shiftNotification.description).isEqualTo(description)
        Assertions.assertThat(shiftNotification.shiftDate).isEqualTo(shiftDate)
        Assertions.assertThat(shiftNotification.lastModifiedDateTime).isEqualTo(lastModified)
        Assertions.assertThat(shiftNotification.notificationType).isEqualTo(notificationType)
        Assertions.assertThat(shiftNotification.acknowledged).isEqualTo(acknowledged)
    }
}