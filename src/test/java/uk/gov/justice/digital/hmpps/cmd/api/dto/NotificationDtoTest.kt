package uk.gov.justice.digital.hmpps.cmd.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.model.ShiftNotification
import uk.gov.justice.digital.hmpps.cmd.api.service.NotificationService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NotificationDtoTest {

    @Test
    fun `Create Notification Dto from collection of ShiftNotification`() {
        val shifts = listOf(getValidShiftNotification())
        val notificationDtos = shifts.map { NotificationDto.from(it, NotificationService.getNotificationDescription(it)) }

        Assertions.assertThat(notificationDtos).hasSize(1)

        val first = notificationDtos[0]
        Assertions.assertThat(first.description).isEqualTo("Your shift on ${dateFormat.format(shifts[0].shiftDate)} has been added")
        Assertions.assertThat(first.shiftModified).isEqualTo(shifts[0].shiftModified)
        Assertions.assertThat(first.processed).isEqualTo(shifts[0].processed)
    }

    @Test
    fun `Create Notification Dto from empty collection of ShiftNotification`() {
        val shifts: List<ShiftNotification> = listOf()
        val notificationDtos = shifts.map { NotificationDto.from(it, NotificationService.getNotificationDescription(it)) }

        Assertions.assertThat(notificationDtos).hasSize(0)
    }

    companion object {

        val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d")

        fun getValidShiftNotification(): ShiftNotification {
            val date = LocalDateTime.now()

            val quantumId = "XYZ"
            val shiftDate = date.plusDays(2)
            val shiftModified = date.plusDays(3)
            val taskStart = 123
            val taskEnd = 456
            val task = "Any Activity"
            val shiftType = "shift"
            val actionType = "add"

            val processed = false

            return ShiftNotification(
                    1L,
                    quantumId,
                    shiftDate,
                    shiftModified,
                    taskStart,
                    taskEnd,
                    task,
                    shiftType,
                    actionType,
                    processed
            )
        }
    }
}