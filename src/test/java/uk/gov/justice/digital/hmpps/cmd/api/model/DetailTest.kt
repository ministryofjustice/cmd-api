package uk.gov.justice.digital.hmpps.cmd.api.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailModificationType
import uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class DetailTest {

  @Test
  fun `Should return a Detail with any values`() {
    val clock =
      Clock.fixed(LocalDate.of(2020, 5, 3).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())

    val quantumId = "XYZ"
    val shiftModified: LocalDateTime = LocalDateTime.now(clock).minusDays(3)
    val shiftDate: LocalDate = LocalDate.now(clock)
    val detailStartTimeInSeconds = 7200L
    val detailEndTimeInSeconds = 84500L
    val shiftType = ShiftType.OVERTIME
    val activity = "Phone Center"
    val detailModificationType = DetailModificationType.EDIT
    val templateName = null

    val overtime = Detail(
      quantumId,
      shiftModified,
      shiftDate,
      shiftType.value,
      detailStartTimeInSeconds,
      detailEndTimeInSeconds,
      activity,
      detailModificationType.value,
      templateName,
    )

    assertThat(overtime.quantumId).isEqualTo(quantumId)
    assertThat(overtime.shiftModified).isEqualTo(shiftModified)
    assertThat(overtime.shiftDate).isEqualTo(shiftDate)
    assertThat(overtime.shiftType).isEqualTo(shiftType.value)
    assertThat(overtime.startTimeInSeconds).isEqualTo(detailStartTimeInSeconds)
    assertThat(overtime.endTimeInSeconds).isEqualTo(detailEndTimeInSeconds)
    assertThat(overtime.activity).isEqualTo(activity)
    assertThat(overtime.actionType).isEqualTo(detailModificationType.value)
    assertThat(overtime.templateName).isEqualTo(templateName)
  }
}
