package uk.gov.justice.digital.hmpps.cmd.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference
import uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftActionType
import uk.gov.justice.digital.hmpps.cmd.api.model.Notification
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

class NotificationDtoTest {

    @Test
    fun `Create Notification Dto from collection of Notification`() {
        val shifts = listOf(getValidNotification())
        val notificationDtos = shifts.map { NotificationDto.from(it, CommunicationPreference.NONE) }

        Assertions.assertThat(notificationDtos).hasSize(1)

        val first = notificationDtos[0]
        Assertions.assertThat(first.description).isEqualTo("Your shift on Sunday, 3rd May has been added.")
        Assertions.assertThat(first.shiftModified).isEqualTo(shifts[0].shiftModified)
        Assertions.assertThat(first.processed).isEqualTo(shifts[0].processed)
    }

    @Test
    fun `Create Notification Dto from collection of ShiftTaskNotification`() {
        val shifts = listOf(getValidShiftTaskNotification())
        val notificationDtos = shifts.map { NotificationDto.from(it, CommunicationPreference.NONE) }

        Assertions.assertThat(notificationDtos).hasSize(1)

        val first = notificationDtos[0]
        Assertions.assertThat(first.description).isEqualTo("Your detail on Sunday, 3rd May (full day) has been added as Any Activity.")
        Assertions.assertThat(first.shiftModified).isEqualTo(shifts[0].shiftModified)
        Assertions.assertThat(first.processed).isEqualTo(shifts[0].processed)
    }

    companion object {

        private val clock = Clock.fixed(LocalDate.of(2020, 5, 3).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())

        fun getValidNotification(): Notification {
            val shiftDate = LocalDate.now(clock)

            val quantumId = "XYZ"
            val shiftModified = shiftDate.atStartOfDay().minusDays(3)
            val taskStart = shiftDate.atStartOfDay()
            val taskEnd = shiftDate.atStartOfDay()
            val task = null
            val shiftType = ShiftType.SHIFT
            val actionType = ShiftActionType.ADD
            val processed = false

            return Notification(
                    1L,
                    quantumId,
                    shiftModified,
                    taskStart,
                    taskEnd,
                    task,
                    shiftType,
                    actionType,
                    processed
            )
        }

        fun getValidShiftTaskNotification(): Notification {
            val shiftDate = LocalDate.now(clock)

            val quantumId = "XYZ"
            val shiftModified = shiftDate.atStartOfDay().minusDays(3)
            val taskStart = shiftDate.atStartOfDay()
            val taskEnd = shiftDate.atStartOfDay()
            val task = "Any Activity"
            val shiftType = ShiftType.SHIFT
            val actionType = ShiftActionType.ADD
            val processed = false

            return Notification(
                    1L,
                    quantumId,
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