package uk.gov.justice.digital.hmpps.cmd.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrDetailDto
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrModifiedDetailDto
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailModificationType
import uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.cmd.api.model.CmdNotification
import uk.gov.justice.digital.hmpps.cmd.api.model.Detail
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class CsrDetailDtoTest {

  @Test
  fun `Create Overtime Dto`() {
    val detail = getFullyPopulatedDetail()
    val start = LocalDate.now(clock).atTime(LocalTime.MIN)
    val end = LocalDate.now(clock).atTime(LocalTime.parse("23:59"))
    val detailDto = CsrDetailDto.from(detail)

    Assertions.assertThat(detailDto.shiftType).isEqualTo(shiftType)
    Assertions.assertThat(detailDto.detailStart).isEqualTo(start)
    Assertions.assertThat(detailDto.detailEnd).isEqualTo(end)
    Assertions.assertThat(detailDto.activity).isEqualTo(activity)
  }

  @Test
  fun `Create Overtime Dto even if values are null`() {
    val detail = Detail(
      null,
      null,
      LocalDate.now(clock),
      ShiftType.SHIFT.value,
      null,
      null,
      null,
      null,
      null,
    )
    val start = LocalDate.now(clock).atTime(LocalTime.MIN)
    val detailDto = CsrDetailDto.from(detail)

    Assertions.assertThat(detailDto.detailStart).isEqualTo(start)
    Assertions.assertThat(detailDto.detailEnd).isEqualTo(start)
    Assertions.assertThat(detailDto.activity).isNull()
  }

  @Test
  fun `Create Dto from Shift CmdNotification`() {
    val detail = CmdNotification(
      id = 101,
      levelId = 1000,
      onDate = LocalDate.parse("2022-04-01"),
      quantumId = "A-USER",
      lastModified = LocalDateTime.parse("2022-03-30T15:23:00"),
      actionType = 47015,
      startTimeInSeconds = 3600,
      endTimeInSeconds = 7200,
      activity = null,
    )

    val detailDto = CsrModifiedDetailDto.from(detail)

    Assertions.assertThat(detailDto).isEqualTo(
      CsrModifiedDetailDto(
        id = 101,
        quantumId = "A-USER",
        shiftModified = LocalDateTime.parse("2022-03-30T15:23:00"),
        shiftType = ShiftType.SHIFT,
        detailStart = LocalDateTime.parse("2022-04-01T01:00:00"),
        detailEnd = LocalDateTime.parse("2022-04-01T02:00:00"),
        activity = null,
        actionType = DetailModificationType.ADD,
      ),
    )
  }

  @Test
  fun `Create Dto from Detail CmdNotification`() {
    val detail = CmdNotification(
      id = 101,
      levelId = 4000,
      onDate = LocalDate.parse("2022-04-01"),
      quantumId = "A-USER",
      lastModified = LocalDateTime.parse("2022-03-30T15:23:00"),
      // this means database value was null in a resultSet!/
      actionType = 0,
      startTimeInSeconds = 3600,
      endTimeInSeconds = 7200,
      activity = "CCTV Monitoring",
    )

    val detailDto = CsrModifiedDetailDto.from(detail)

    Assertions.assertThat(detailDto).isEqualTo(
      CsrModifiedDetailDto(
        id = 101,
        quantumId = "A-USER",
        shiftModified = LocalDateTime.parse("2022-03-30T15:23:00"),
        shiftType = ShiftType.OVERTIME,
        detailStart = LocalDateTime.parse("2022-04-01T01:00:00"),
        detailEnd = LocalDateTime.parse("2022-04-01T02:00:00"),
        activity = "CCTV Monitoring",
        actionType = DetailModificationType.EDIT,
      ),
    )
  }

  companion object {
    private val clock =
      Clock.fixed(LocalDate.of(2020, 5, 3).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())

    private val shiftDate: LocalDate = LocalDate.now(clock)
    private val detailStartTimeInSeconds = 0L
    private val detailEndTimeInSeconds = 23 * 60 * 60 + 59 * 60L

    private val quantumId = "XYZ"
    private val shiftModified: LocalDateTime = LocalDateTime.now(clock).minusDays(3)
    private val shiftType = ShiftType.OVERTIME
    private val actionType = DetailModificationType.EDIT
    private val activity = "Phone Center"

    fun getFullyPopulatedDetail(): Detail = Detail(
      quantumId,
      shiftModified,
      shiftDate,
      shiftType.value,
      detailStartTimeInSeconds,
      detailEndTimeInSeconds,
      activity,
      actionType.value,
      null,
    )
  }
}
