package uk.gov.justice.digital.hmpps.cmd.api.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrClient
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrDetailDto
import uk.gov.justice.digital.hmpps.cmd.api.domain.FullDayActivityType
import uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.cmd.api.domain.TaskDisplayType
import uk.gov.justice.digital.hmpps.cmd.api.model.Prison
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Optional

@DisplayName("Shift Service tests")
internal class ShiftServiceTestTask {
  private val csrApiClient: CsrClient = mock()
  private val prisonService: PrisonService = mock()
  private val authenticationFacade: HmppsAuthenticationHolder = mock()
  private val clock = Clock.fixed(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
  private val service = ShiftService(prisonService, csrApiClient, clock, authenticationFacade)

  private val day1 = LocalDate.now(clock)
  private val day2 = day1.plusDays(1)
  private val validNightShifts = listOf(
    CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(20, 15)), day2.atTime(LocalTime.of(12, 30)), "Nights OSG"),
  )
  private val validShifts = listOf(
    CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(7, 15)), day1.atTime(LocalTime.of(12, 30)), "Present"),
    CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(12, 30)), day1.atTime(LocalTime.of(13, 30)), "Break (Unpaid)"),
    CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(13, 30)), day1.atTime(LocalTime.of(17, 0)), "Present"),
  )

  @BeforeEach
  fun resetAllMocks() {
    reset(csrApiClient, prisonService)

    whenever(authenticationFacade.username).thenReturn("xyz")
    whenever(prisonService.getPrisonForUser()).thenReturn(Prison("prison", "", "", 1))
  }

  @Nested
  @DisplayName("Get Shift tests")
  inner class GetShiftTest {

    @Test
    fun `Should default to today if no dates`() {
      whenever(csrApiClient.getDetailsForUser(day1, day1, 1, "xyz")).thenReturn(validShifts)

      val dayModel = service.getDetailsForUser(Optional.empty(), Optional.empty())

      verify(prisonService).getPrisonForUser()
      verify(csrApiClient).getDetailsForUser(day1, day1, 1, "xyz")

      assertThat(dayModel.first().date).isEqualTo(day1)
    }

    @Test
    fun `Should not modify passed in dates`() {
      whenever(prisonService.getPrisonForUser()).thenReturn(Prison("prison", "", "", 1))
      whenever(csrApiClient.getDetailsForUser(day1, day1, 1, "xyz")).thenReturn(validShifts)

      val dayModel = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify(prisonService).getPrisonForUser()
      verify(csrApiClient).getDetailsForUser(day1, day1, 1, "xyz")

      assertThat(dayModel)
    }

    @Test
    fun `Should return 'no day' for no task data`() {
      whenever(csrApiClient.getDetailsForUser(day1, day1, 1, "xyz")).thenReturn(listOf())

      val dayModel = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify(prisonService).getPrisonForUser()
      verify(csrApiClient).getDetailsForUser(day1, day1, 1, "xyz")

      assertThat(dayModel.first().date).isEqualTo(day1)
      assertThat(dayModel.first().shiftType).isEqualTo(FullDayActivityType.NONE)
    }

    @Test
    fun `Should return 'shift' for shift data`() {
      whenever(csrApiClient.getDetailsForUser(day1, day1, 1, "xyz")).thenReturn(validShifts)

      val dayModel = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify(prisonService).getPrisonForUser()
      verify(csrApiClient).getDetailsForUser(day1, day1, 1, "xyz")

      assertThat(dayModel.first().date).isEqualTo(day1)
      assertThat(dayModel.first().shiftType).isEqualTo(FullDayActivityType.SHIFT)
    }

    @Test
    fun `Should return 'shift' for overtime data`() {
      val shifts = listOf(
        CsrDetailDto(ShiftType.OVERTIME, day1.atTime(LocalTime.of(7, 15)), day1.atTime(LocalTime.of(12, 30)), "Present"),
        CsrDetailDto(ShiftType.OVERTIME, day1.atTime(LocalTime.of(12, 30)), day1.atTime(LocalTime.of(13, 30)), "Break (Unpaid)"),
        CsrDetailDto(ShiftType.OVERTIME, day1.atTime(LocalTime.of(13, 30)), day1.atTime(LocalTime.of(17, 0)), "Present"),
      )

      whenever(csrApiClient.getDetailsForUser(day1, day1, 1, "xyz")).thenReturn(shifts)

      val dayModel = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify(prisonService).getPrisonForUser()
      verify(csrApiClient).getDetailsForUser(day1, day1, 1, "xyz")

      assertThat(dayModel.first().date).isEqualTo(day1)
      assertThat(dayModel.first().shiftType).isEqualTo(FullDayActivityType.SHIFT)
    }

    @Test
    fun `Should not return things for a different dates`() {
      whenever(csrApiClient.getDetailsForUser(day2, day2, 1, "xyz")).thenReturn(validShifts)

      val dayModel = service.getDetailsForUser(Optional.of(day2), Optional.of(day2))

      verify(prisonService).getPrisonForUser()
      verify(csrApiClient).getDetailsForUser(day2, day2, 1, "xyz")

      assertThat(dayModel.first().date).isEqualTo(day2)
      assertThat(dayModel.first().shiftType).isEqualTo(FullDayActivityType.NONE)
      assertThat(dayModel.first().details).hasSize(0)
    }

    @Test
    fun `Should identify Shift start`() {
      whenever(csrApiClient.getDetailsForUser(day1, day1, 1, "xyz")).thenReturn(validShifts)

      val dayModel = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify(prisonService).getPrisonForUser()
      verify(csrApiClient).getDetailsForUser(day1, day1, 1, "xyz")

      assertThat(dayModel.first().date).isEqualTo(day1)
      assertThat(dayModel.first().shiftType).isEqualTo(FullDayActivityType.SHIFT)

      val overtimeStartTask = dayModel.first().details.first { it.displayType == TaskDisplayType.DAY_START }
      assertThat(overtimeStartTask.start).isEqualTo(day1.atTime(LocalTime.of(7, 15)))
      assertThat(overtimeStartTask.end).isEqualTo(day1.atTime(LocalTime.of(12, 30)))
      assertThat(overtimeStartTask.displayTypeTime).isEqualTo(day1.atTime(LocalTime.of(7, 15)))
      assertThat(overtimeStartTask.finishDuration).isNull()
    }

    @Test
    fun `Should identify Shift end`() {
      whenever(csrApiClient.getDetailsForUser(day1, day1, 1, "xyz")).thenReturn(validShifts)

      val dayModel = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify(prisonService).getPrisonForUser()
      verify(csrApiClient).getDetailsForUser(day1, day1, 1, "xyz")

      assertThat(dayModel.first().date).isEqualTo(day1)
      assertThat(dayModel.first().shiftType).isEqualTo(FullDayActivityType.SHIFT)

      val overtimeStartTask = dayModel.first().details.first { it.displayType == TaskDisplayType.DAY_FINISH }
      assertThat(overtimeStartTask.start).isEqualTo(day1.atTime(LocalTime.of(13, 30)))
      assertThat(overtimeStartTask.end).isEqualTo(day1.atTime(LocalTime.of(17, 0)))
      assertThat(overtimeStartTask.displayTypeTime).isEqualTo(day1.atTime(LocalTime.of(17, 0)))
      assertThat(overtimeStartTask.finishDuration).isEqualTo(31500L)
    }

    @Test
    fun `Should identify Night Shift start`() {
      whenever(csrApiClient.getDetailsForUser(day1, day1, 1, "xyz")).thenReturn(validNightShifts)

      val dayModel = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify(prisonService).getPrisonForUser()
      verify(csrApiClient).getDetailsForUser(day1, day1, 1, "xyz")

      assertThat(dayModel.first().date).isEqualTo(day1)
      assertThat(dayModel.first().shiftType).isEqualTo(FullDayActivityType.SHIFT)

      val overtimeStartTask = dayModel.first().details.first { it.displayType == TaskDisplayType.NIGHT_START }
      assertThat(overtimeStartTask.start).isEqualTo(day1.atTime(LocalTime.of(20, 15)))
      assertThat(overtimeStartTask.end).isEqualTo(day2.atTime(LocalTime.of(12, 30)))
      assertThat(overtimeStartTask.displayTypeTime).isEqualTo(day1.atTime(LocalTime.of(20, 15)))

      assertThat(overtimeStartTask.finishDuration).isNull()
    }

    @Test
    fun `Should identify Night Shift end`() {
      whenever(csrApiClient.getDetailsForUser(day2, day2, 1, "xyz")).thenReturn(validNightShifts)

      val dayModel = service.getDetailsForUser(Optional.of(day2), Optional.of(day2))

      verify(prisonService).getPrisonForUser()
      verify(csrApiClient).getDetailsForUser(day2, day2, 1, "xyz")

      assertThat(dayModel.first().date).isEqualTo(day2)
      assertThat(dayModel.first().shiftType).isEqualTo(FullDayActivityType.SHIFT)

      val overtimeStartTask = dayModel.first().details.first { it.displayType == TaskDisplayType.NIGHT_FINISH }
      assertThat(overtimeStartTask.start).isEqualTo(day1.atTime(LocalTime.of(20, 15)))
      assertThat(overtimeStartTask.end).isEqualTo(day2.atTime(LocalTime.of(12, 30)))
      assertThat(overtimeStartTask.displayTypeTime).isEqualTo(day2.atTime(LocalTime.of(12, 30)))
      assertThat(overtimeStartTask.finishDuration).isEqualTo(58500L)
    }

    @Test
    fun `Should identify Overtime Night Shift start`() {
      val shifts = listOf(
        CsrDetailDto(ShiftType.OVERTIME, day1.atTime(LocalTime.of(20, 15)), day2.atTime(LocalTime.of(12, 30)), "Nights OSG"),
      )

      whenever(csrApiClient.getDetailsForUser(day1, day1, 1, "xyz")).thenReturn(shifts)

      val dayModel = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify(prisonService).getPrisonForUser()
      verify(csrApiClient).getDetailsForUser(day1, day1, 1, "xyz")

      assertThat(dayModel.first().date).isEqualTo(day1)
      assertThat(dayModel.first().shiftType).isEqualTo(FullDayActivityType.SHIFT)

      val overtimeStartTask = dayModel.first().details.first { it.displayType == TaskDisplayType.OVERTIME_NIGHT_START }
      assertThat(overtimeStartTask.start).isEqualTo(day1.atTime(LocalTime.of(20, 15)))
      assertThat(overtimeStartTask.end).isEqualTo(day2.atTime(LocalTime.of(12, 30)))
      assertThat(overtimeStartTask.displayTypeTime).isEqualTo(day1.atTime(LocalTime.of(20, 15)))
      assertThat(overtimeStartTask.finishDuration).isNull()
    }

    @Test
    fun `Should identify Overtime Night Shift end`() {
      val shifts = listOf(
        CsrDetailDto(ShiftType.OVERTIME, day1.atTime(LocalTime.of(20, 15)), day2.atTime(LocalTime.of(12, 30)), "Nights OSG"),
      )
      whenever(csrApiClient.getDetailsForUser(day2, day2, 1, "xyz")).thenReturn(shifts)

      val dayModel = service.getDetailsForUser(Optional.of(day2), Optional.of(day2))

      verify(prisonService).getPrisonForUser()
      verify(csrApiClient).getDetailsForUser(day2, day2, 1, "xyz")

      assertThat(dayModel.first().date).isEqualTo(day2)
      assertThat(dayModel.first().shiftType).isEqualTo(FullDayActivityType.SHIFT)

      val overtimeStartTask = dayModel.first().details.first { it.displayType == TaskDisplayType.OVERTIME_NIGHT_FINISH }
      assertThat(overtimeStartTask.start).isEqualTo(day1.atTime(LocalTime.of(20, 15)))
      assertThat(overtimeStartTask.end).isEqualTo(day2.atTime(LocalTime.of(12, 30)))
      assertThat(overtimeStartTask.displayTypeTime).isEqualTo(day2.atTime(LocalTime.of(12, 30)))
      assertThat(overtimeStartTask.finishDuration).isEqualTo(58500L)
    }
  }

  @Nested
  @DisplayName("Finish Duration specific tests")
  inner class CheckFinishDuration {

    @Test
    fun `Should calculate duration correctly with two night shifts in a row`() {
      val day2 = day1.plusDays(1)
      val day3 = day1.plusDays(2)
      val shifts = listOf(
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(20, 15)), day2.atTime(LocalTime.of(12, 30)), "Nights OSG"),
        CsrDetailDto(ShiftType.SHIFT, day2.atTime(LocalTime.of(20, 15)), day3.atTime(LocalTime.of(12, 30)), "Nights OSG"),
      )

      whenever(prisonService.getPrisonForUser()).thenReturn(Prison("prison", "", "", 1))
      whenever(csrApiClient.getDetailsForUser(day2, day2, 1, "xyz")).thenReturn(shifts)

      val dayModel = service.getDetailsForUser(Optional.of(day2), Optional.of(day2))

      verify(prisonService).getPrisonForUser()
      verify(csrApiClient).getDetailsForUser(day2, day2, 1, "xyz")

      assertThat(dayModel.first().date).isEqualTo(day2)
      assertThat(dayModel.first().shiftType).isEqualTo(FullDayActivityType.SHIFT)

      val overtimeStartTask = dayModel.first().details.first { it.displayType == TaskDisplayType.NIGHT_FINISH }
      assertThat(overtimeStartTask.start).isEqualTo(day1.atTime(LocalTime.of(20, 15)))
      assertThat(overtimeStartTask.end).isEqualTo(day2.atTime(LocalTime.of(12, 30)))
      assertThat(overtimeStartTask.displayTypeTime).isEqualTo(day2.atTime(LocalTime.of(12, 30)))
      assertThat(overtimeStartTask.finishDuration).isEqualTo(58500L)
    }
  }
}
