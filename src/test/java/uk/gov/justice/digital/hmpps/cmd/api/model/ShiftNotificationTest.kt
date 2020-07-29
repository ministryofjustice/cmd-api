package uk.gov.justice.digital.hmpps.cmd.api.model

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.client.dto.ShiftNotificationDto
import java.time.LocalDateTime

class ShiftNotificationTest {

    @Test
    fun `Should return a valid shift notification`() {
        val date = LocalDateTime.now()

        val quantumId = "XYZ"
        val shiftDate = date.plusDays(2)
        val shiftModified = date.plusDays(3)
        val taskStart = 123L
        val taskEnd = 456L
        val task = "Any Activity"
        val shiftType = "SHIFT"
        val actionType = "ADD"

        val processed = false

        val shiftNotification = ShiftNotification(
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

        Assertions.assertThat(shiftNotification.quantumId).isEqualTo(quantumId)
        Assertions.assertThat(shiftNotification.shiftDate).isEqualTo(shiftDate)
        Assertions.assertThat(shiftNotification.shiftModified).isEqualTo(shiftModified)
        Assertions.assertThat(shiftNotification.taskStart).isEqualTo(taskStart)
        Assertions.assertThat(shiftNotification.taskEnd).isEqualTo(taskEnd)
        Assertions.assertThat(shiftNotification.task).isEqualTo(task)
        Assertions.assertThat(shiftNotification.shiftType).isEqualTo(shiftType)
        Assertions.assertThat(shiftNotification.actionType).isEqualTo(actionType)
        Assertions.assertThat(shiftNotification.processed).isEqualTo(processed)
    }

    @Test
    fun `Can convert from DTO`() {
        val date = LocalDateTime.now()

        val quantumId = "XYZ"
        val shiftDate = date.plusDays(2)
        val shiftModified = date.plusDays(3)
        val taskStart = 123L
        val taskEnd = 456L
        val task = "Any Activity"
        val shiftType = "SHIFT"
        val actionType = "ADD"

        val processed = false

        val shiftNotificationDto = ShiftNotificationDto(
                1L,
                quantumId,
                shiftDate,
                shiftModified,
                taskStart,
                taskEnd,
                task,
                shiftType,
                actionType
        )

        val shiftNotification = ShiftNotification.fromDto(shiftNotificationDto)

        Assertions.assertThat(shiftNotification.quantumId).isEqualTo(quantumId)
        Assertions.assertThat(shiftNotification.shiftDate).isEqualTo(shiftDate)
        Assertions.assertThat(shiftNotification.shiftModified).isEqualTo(shiftModified)
        Assertions.assertThat(shiftNotification.taskStart).isEqualTo(taskStart)
        Assertions.assertThat(shiftNotification.taskEnd).isEqualTo(taskEnd)
        Assertions.assertThat(shiftNotification.task).isEqualTo(task)
        Assertions.assertThat(shiftNotification.shiftType).isEqualTo(shiftType)
        Assertions.assertThat(shiftNotification.actionType).isEqualTo(actionType)
        Assertions.assertThat(shiftNotification.processed).isEqualTo(processed)
    }
}