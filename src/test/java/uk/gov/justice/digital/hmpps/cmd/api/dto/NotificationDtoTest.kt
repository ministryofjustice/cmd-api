package uk.gov.justice.digital.hmpps.cmd.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.model.ShiftNotification
import uk.gov.justice.digital.hmpps.cmd.api.model.ShiftTaskNotification
import java.time.LocalDateTime

class NotificationDtoTest {

    @Test
    fun `Create Notification Dto from collection of ShiftNotification`() {
        val shifts = listOf(getValidShiftNotification())
        val notificationDtos = NotificationDto.fromShifts(shifts)

        Assertions.assertThat(notificationDtos).hasSize(1)

        val first = notificationDtos[0]
        Assertions.assertThat(first.description).isEqualTo(shifts[0].description)
        Assertions.assertThat(first.lastModified).isEqualTo(shifts[0].lastModifiedDateTime)
        Assertions.assertThat(first.acknowledged).isEqualTo(shifts[0].acknowledged)
    }

    @Test
    fun `Create Notification Dto from empty collection of ShiftNotification`() {
        val shifts: List<ShiftNotification> = listOf()
        val notificationDtos = NotificationDto.fromShifts(shifts)

        Assertions.assertThat(notificationDtos).hasSize(0)
    }

    @Test
    fun `Create Notification Dto from collection of ShiftTaskNotification`() {
        val shifts = listOf(getValidShiftTaskNotification())
        val notificationDtos = NotificationDto.fromTasks(shifts)

        Assertions.assertThat(notificationDtos).hasSize(1)

        val first = notificationDtos[0]
        Assertions.assertThat(first.description).isEqualTo(shifts[0].description)
        Assertions.assertThat(first.lastModified).isEqualTo(shifts[0].lastModifiedDateTime)
        Assertions.assertThat(first.acknowledged).isEqualTo(shifts[0].acknowledged)
    }

    @Test
    fun `Create Notification Dto from empty collection of ShiftTaskNotification`() {
        val shifts: List<ShiftTaskNotification> = listOf()
        val notificationDtos = NotificationDto.fromTasks(shifts)

        Assertions.assertThat(notificationDtos).hasSize(0)
    }

    companion object {
        fun getValidShiftNotification(): ShiftNotification {
            val date = LocalDateTime.now()

            val quantumId = "XYZ"
            val description = "Any Description,"
            val shiftDate = date.plusDays(2)
            val lastModified = date.plusDays(3)
            val notificationType = 0L
            val acknowledged = false

            return ShiftNotification(
                    quantumId,
                    description,
                    shiftDate,
                    lastModified,
                    notificationType,
                    acknowledged
            )
        }

        fun getValidShiftTaskNotification(): ShiftTaskNotification {
            val date = LocalDateTime.now()

            val quantumId = "XYZ"
            val description = "Any Description,"
            val taskDate = date.plusDays(2)
            val taskStartSec = 123
            val taskEndSec = 456
            val activity = "Any Activity"
            val lastModified = date.plusDays(3)
            val acknowledged = false

            return ShiftTaskNotification(
                    quantumId,
                    description,
                    taskDate,
                    taskStartSec,
                    taskEndSec,
                    activity,
                    lastModified,
                    acknowledged
            )
        }
    }
}