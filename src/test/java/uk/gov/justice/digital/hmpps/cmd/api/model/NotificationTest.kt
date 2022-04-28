package uk.gov.justice.digital.hmpps.cmd.api.model

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrModifiedDetailDto
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailModificationType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import java.time.LocalDate

class NotificationTest {

  @Test
  fun `Should return a valid shift notification`() {
    val shiftDate = LocalDate.now()

    val quantumId = "XYZ"
    val shiftModified = shiftDate.plusDays(3).atStartOfDay().minusDays(3)
    val taskStart = shiftDate.atStartOfDay().plusSeconds(123L)
    val taskEnd = shiftDate.atStartOfDay().plusSeconds(456L)
    val task = "Any Activity"
    val shiftType = ShiftType.SHIFT
    val actionType = DetailModificationType.ADD
    val processed = false

    val notification = Notification(
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

    Assertions.assertThat(notification.quantumId).isEqualTo(quantumId)
    Assertions.assertThat(notification.shiftModified).isEqualTo(shiftModified)
    Assertions.assertThat(notification.detailStart).isEqualTo(taskStart)
    Assertions.assertThat(notification.detailEnd).isEqualTo(taskEnd)
    Assertions.assertThat(notification.activity).isEqualTo(task)
    Assertions.assertThat(notification.parentType).isEqualTo(shiftType)
    Assertions.assertThat(notification.actionType).isEqualTo(actionType)
    Assertions.assertThat(notification.processed).isEqualTo(processed)
  }

  @Test
  fun `Can convert from DTO`() {
    val shiftDate = LocalDate.now()

    val quantumId = "XYZ"
    val shiftModified = shiftDate.plusDays(3).atStartOfDay().minusDays(3)
    val taskStart = shiftDate.atStartOfDay().plusSeconds(123L)
    val taskEnd = shiftDate.atStartOfDay().plusSeconds(456L)
    val task = "Any Activity"
    val shiftType = ShiftType.SHIFT
    val actionType = DetailModificationType.ADD
    val processed = false

    val shiftNotificationDto = CsrModifiedDetailDto(
      quantumId,
      shiftModified,
      shiftType,
      taskStart,
      taskEnd,
      task,
      actionType
    )

    val shiftNotification = Notification.fromDto(shiftNotificationDto)

    Assertions.assertThat(shiftNotification.quantumId).isEqualTo(quantumId)
    Assertions.assertThat(shiftNotification.shiftModified).isEqualTo(shiftModified)
    Assertions.assertThat(shiftNotification.detailStart).isEqualTo(taskStart)
    Assertions.assertThat(shiftNotification.detailEnd).isEqualTo(taskEnd)
    Assertions.assertThat(shiftNotification.activity).isEqualTo(task)
    Assertions.assertThat(shiftNotification.parentType).isEqualTo(shiftType)
    Assertions.assertThat(shiftNotification.actionType).isEqualTo(actionType)
    Assertions.assertThat(shiftNotification.processed).isEqualTo(processed)
  }

  @Test
  fun `Can convert a collection from DTO`() {
    val shiftDate = LocalDate.now()

    val quantumId = "XYZ"
    val shiftModified = shiftDate.plusDays(3).atStartOfDay().minusDays(3)
    val taskStart = shiftDate.atStartOfDay().plusSeconds(123L)
    val taskEnd = shiftDate.atStartOfDay().plusSeconds(456L)
    val task = "Any Activity"
    val shiftType = ShiftType.SHIFT
    val actionType = DetailModificationType.ADD
    val processed = false

    val shiftNotificationDto = CsrModifiedDetailDto(
      quantumId,
      shiftModified,
      shiftType,
      taskStart,
      taskEnd,
      task,
      actionType
    )

    val lisfOfDtos = listOf(shiftNotificationDto)

    val shiftNotifications = Notification.fromDto(lisfOfDtos)
    val shiftNotification = shiftNotifications.elementAt(0)

    Assertions.assertThat(shiftNotification.quantumId).isEqualTo(quantumId)
    Assertions.assertThat(shiftNotification.shiftModified).isEqualTo(shiftModified)
    Assertions.assertThat(shiftNotification.detailStart).isEqualTo(taskStart)
    Assertions.assertThat(shiftNotification.detailEnd).isEqualTo(taskEnd)
    Assertions.assertThat(shiftNotification.activity).isEqualTo(task)
    Assertions.assertThat(shiftNotification.parentType).isEqualTo(shiftType)
    Assertions.assertThat(shiftNotification.actionType).isEqualTo(actionType)
    Assertions.assertThat(shiftNotification.processed).isEqualTo(processed)
  }
}
