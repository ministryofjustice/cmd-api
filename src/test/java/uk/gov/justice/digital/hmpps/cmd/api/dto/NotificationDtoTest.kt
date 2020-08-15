package uk.gov.justice.digital.hmpps.cmd.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.model.ShiftNotification
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class NotificationDtoTest {

    @Test
    fun `Create Notification Dto from collection of ShiftNotification`() {
        val shifts = listOf(getValidShiftNotification())
        val notificationDtos = shifts.map { NotificationDto.from(it, "Any Description") }

        Assertions.assertThat(notificationDtos).hasSize(1)

        val first = notificationDtos[0]
        Assertions.assertThat(first.description).isEqualTo("Any Description")
        Assertions.assertThat(first.shiftModified).isEqualTo(shifts[0].shiftModified)
        Assertions.assertThat(first.processed).isEqualTo(shifts[0].processed)
    }

    @Test
    fun `Create Notification Dto from collection of ShiftTaskNotification`() {
        val shifts = listOf(getValidShiftTaskNotification())
        val notificationDtos = shifts.map { NotificationDto.from(it,"Any Description") }

        Assertions.assertThat(notificationDtos).hasSize(1)

        val first = notificationDtos[0]
        Assertions.assertThat(first.description).isEqualTo("Any Description")
        Assertions.assertThat(first.shiftModified).isEqualTo(shifts[0].shiftModified)
        Assertions.assertThat(first.processed).isEqualTo(shifts[0].processed)
    }

    companion object {

        private val clock = Clock.fixed(LocalDate.of(2020, 5, 3).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())

        fun getValidShiftNotification(): ShiftNotification {
            val shiftDate = LocalDateTime.now(clock)

            val quantumId = "XYZ"
            val shiftModified = shiftDate.minusDays(3)
            val taskStart = 0L
            val taskEnd = 0L
            val task = ""
            val shiftType = "shift"
            val actionType = "add"

            val processed = false

            return ShiftNotification(
                    1L,
                    quantumId,
                    shiftDate.toLocalDate(),
                    shiftModified,
                    taskStart,
                    taskEnd,
                    task,
                    shiftType,
                    actionType,
                    processed
            )
        }

        fun getValidShiftTaskNotification(): ShiftNotification {
            val shiftDate = LocalDateTime.now(clock)

            val quantumId = "XYZ"
            val shiftModified = shiftDate.minusDays(3)
            val taskStart = 123L
            val taskEnd = 456L
            val task = "Any Activity"
            val shiftType = "shift"
            val actionType = "add"

            val processed = false

            return ShiftNotification(
                    1L,
                    quantumId,
                    shiftDate.toLocalDate(),
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