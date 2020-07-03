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
        val notificationDtos = NotificationDto.fromShift(shifts)

        Assertions.assertThat(notificationDtos).hasSize(1)

        val first = notificationDtos.get(0)
        Assertions.assertThat(first.description).isEqualTo(shifts.get(0).description)
        Assertions.assertThat(first.lastModified).isEqualTo(shifts.get(0).lastModifiedDateTime)
        Assertions.assertThat(first.read).isEqualTo(shifts.get(0).read)
    }

    @Test
    fun `Create Notification Dto from empty collection of ShiftNotification`() {
        val shifts: List<ShiftNotification> = listOf()
        val notificationDtos = NotificationDto.fromShift(shifts)

        Assertions.assertThat(notificationDtos).hasSize(0)
    }

    @Test
    fun `Create Notification Dto from collection of ShiftTaskNotification`() {
        val shifts = listOf(getValidShiftTaskNotification())
        val notificationDtos = NotificationDto.fromTask(shifts)

        Assertions.assertThat(notificationDtos).hasSize(1)

        val first = notificationDtos.get(0)
        Assertions.assertThat(first.description).isEqualTo(shifts.get(0).description)
        Assertions.assertThat(first.lastModified).isEqualTo(shifts.get(0).lastModifiedDateTime)
        Assertions.assertThat(first.read).isEqualTo(shifts.get(0).read)
    }

    @Test
    fun `Create Notification Dto from empty collection of ShiftTaskNotification`() {
        val shifts: List<ShiftTaskNotification> = listOf()
        val notificationDtos = NotificationDto.fromTask(shifts)

        Assertions.assertThat(notificationDtos).hasSize(0)
    }

    companion object {
        fun getValidShiftNotification(): ShiftNotification {
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
            val notificationType = 0L

            return ShiftNotification(
                    quantumId,
                    dateTime,
                    description,
                    shiftDate,
                    lastModified,
                    read,
                    sentSms,
                    sentEmail,
                    lastModSec,
                    notificationType

            )
        }

        fun getValidShiftTaskNotification(): ShiftTaskNotification {
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

            return ShiftTaskNotification(
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
        }
    }
}