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

@DisplayName("Shift Service tests with Overtime")
internal class ShiftServiceTestOvertimeScenarios {
  private val csrApiClient: CsrClient = mock()
  private val prisonService: PrisonService = mock()
  private val authenticationFacade: HmppsAuthenticationHolder = mock()
  private val clock = Clock.fixed(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
  private val service = ShiftService(prisonService, csrApiClient, clock, authenticationFacade)

  private val day1 = LocalDate.now(clock)
  private val day2 = day1.plusDays(1)

  @BeforeEach
  fun resetAllMocks() {
    reset(csrApiClient, prisonService)

    whenever(authenticationFacade.username).thenReturn("xyz")
  }

  @Nested
  @DisplayName("Get Shift tests")
  inner class GetPrisonsTest {

    @Test
    fun `Should return a basic day shift with overtime after`() {
      val dayShift = listOf(
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(7, 15)), day1.atTime(LocalTime.of(12, 30)), "Present"),
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(12, 30)), day1.atTime(LocalTime.of(13, 30)), "Break (Unpaid)"),
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(13, 30)), day1.atTime(LocalTime.of(17, 0)), "Present"),
      )

      val overtime = listOf(
        CsrDetailDto(ShiftType.OVERTIME, day1.atTime(LocalTime.of(17, 15)), day1.atTime(LocalTime.of(18, 30)), "Present"),
        CsrDetailDto(ShiftType.OVERTIME, day1.atTime(LocalTime.of(18, 30)), day1.atTime(LocalTime.of(19, 30)), "Break (Unpaid)"),
        CsrDetailDto(ShiftType.OVERTIME, day1.atTime(LocalTime.of(19, 30)), day1.atTime(LocalTime.of(22, 0)), "Present"),
      )

      whenever(prisonService.getPrisonForUser()).thenReturn(Prison("prison", "", "", 1))
      whenever(csrApiClient.getDetailsForUser(day1, day1, 1, "xyz")).thenReturn(dayShift + overtime)

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify(prisonService).getPrisonForUser()
      verify(csrApiClient).getDetailsForUser(day1, day1, 1, "xyz")

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
      assertThat(dayModel.details).hasSize(6)
      // 4 significant events but 6 details total
      assertThat(dayModel.details.filter { it.displayType != null }).hasSize(4)

      val startTask = dayModel.details.first { it.displayType == TaskDisplayType.DAY_START }
      assertThat(startTask.displayTypeTime).isEqualTo(day1.atTime(LocalTime.of(7, 15)))
      assertThat(startTask.finishDuration).isNull()

      val endTask = dayModel.details.first { it.displayType == TaskDisplayType.DAY_FINISH }
      assertThat(endTask.displayTypeTime).isEqualTo(day1.atTime(LocalTime.of(17, 0)))
      assertThat(endTask.finishDuration).isEqualTo(31500L)

      val overtimeStartTask = dayModel.details.first { it.displayType == TaskDisplayType.OVERTIME_DAY_START }
      assertThat(overtimeStartTask.displayTypeTime).isEqualTo(day1.atTime(LocalTime.of(17, 15)))
      assertThat(overtimeStartTask.finishDuration).isNull()

      val overtimeEndTask = dayModel.details.first { it.displayType == TaskDisplayType.OVERTIME_DAY_FINISH }
      assertThat(overtimeEndTask.displayTypeTime).isEqualTo(day1.atTime(LocalTime.of(22, 0)))
      assertThat(overtimeEndTask.finishDuration).isEqualTo(13500L)
    }

    @Test
    fun `Should return a basic day shift with overtime before`() {
      val dayShift = listOf(
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(17, 15)), day1.atTime(LocalTime.of(18, 30)), "Present"),
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(18, 30)), day1.atTime(LocalTime.of(19, 30)), "Break (Unpaid)"),
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(19, 30)), day1.atTime(LocalTime.of(22, 0)), "Present"),
      )

      val overtime = listOf(
        CsrDetailDto(ShiftType.OVERTIME, day1.atTime(LocalTime.of(7, 15)), day1.atTime(LocalTime.of(12, 30)), "Present"),
        CsrDetailDto(ShiftType.OVERTIME, day1.atTime(LocalTime.of(12, 30)), day1.atTime(LocalTime.of(13, 30)), "Break (Unpaid)"),
        CsrDetailDto(ShiftType.OVERTIME, day1.atTime(LocalTime.of(13, 30)), day1.atTime(LocalTime.of(17, 0)), "Present"),
      )

      whenever(prisonService.getPrisonForUser()).thenReturn(Prison("prison", "", "", 1))
      whenever(csrApiClient.getDetailsForUser(day1, day1, 1, "xyz")).thenReturn(dayShift + overtime)

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify(prisonService).getPrisonForUser()
      verify(csrApiClient).getDetailsForUser(day1, day1, 1, "xyz")

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
      assertThat(dayModel.details).hasSize(6)
      // 4 significant events but 6 details total
      assertThat(dayModel.details.filter { it.displayType != null }).hasSize(4)

      val overtimeStartTask = dayModel.details.first { it.displayType == TaskDisplayType.DAY_START }
      assertThat(overtimeStartTask.displayTypeTime).isEqualTo(day1.atTime(LocalTime.of(17, 15)))
      assertThat(overtimeStartTask.finishDuration).isNull()

      val overtimeEndTask = dayModel.details.first { it.displayType == TaskDisplayType.DAY_FINISH }
      assertThat(overtimeEndTask.displayTypeTime).isEqualTo(day1.atTime(LocalTime.of(22, 0)))
      assertThat(overtimeEndTask.finishDuration).isEqualTo(13500L)

      val startTask = dayModel.details.first { it.displayType == TaskDisplayType.OVERTIME_DAY_START }
      assertThat(startTask.displayTypeTime).isEqualTo(day1.atTime(LocalTime.of(7, 15)))
      assertThat(startTask.finishDuration).isNull()

      val endTask = dayModel.details.first { it.displayType == TaskDisplayType.OVERTIME_DAY_FINISH }
      assertThat(endTask.displayTypeTime).isEqualTo(day1.atTime(LocalTime.of(17, 0)))
      assertThat(endTask.finishDuration).isEqualTo(31500L)
    }

    @Test
    fun `Should return a basic night shift with overtime after`() {
      val nightShift = listOf(
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(20, 45)), day2.atTime(LocalTime.of(7, 45)), "Night OSG"),
      )

      val overtime = listOf(
        CsrDetailDto(ShiftType.OVERTIME, day2.atTime(LocalTime.of(17, 15)), day2.atTime(LocalTime.of(18, 30)), "Present"),
        CsrDetailDto(ShiftType.OVERTIME, day2.atTime(LocalTime.of(18, 30)), day2.atTime(LocalTime.of(19, 30)), "Break (Unpaid)"),
        CsrDetailDto(ShiftType.OVERTIME, day2.atTime(LocalTime.of(19, 30)), day2.atTime(LocalTime.of(22, 0)), "Present"),
      )

      whenever(prisonService.getPrisonForUser()).thenReturn(Prison("prison", "", "", 1))
      whenever(csrApiClient.getDetailsForUser(day1, day2, 1, "xyz")).thenReturn(nightShift + overtime)

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day2))

      verify(prisonService).getPrisonForUser()
      verify(csrApiClient).getDetailsForUser(day1, day2, 1, "xyz")

      assertThat(dayModelList).hasSize(2)

      val dayModel1 = dayModelList.first { it.date == day1 }
      assertThat(dayModel1.date).isEqualTo(day1)
      assertThat(dayModel1.shiftType).isEqualTo(FullDayActivityType.SHIFT)
      assertThat(dayModel1.details).hasSize(1)

      val startTask1 = dayModel1.details.first { it.displayType == TaskDisplayType.NIGHT_START }
      assertThat(startTask1.displayTypeTime).isEqualTo(day1.atTime(LocalTime.of(20, 45)))
      assertThat(startTask1.finishDuration).isNull()

      val dayModel2 = dayModelList.first { it.date == day2 }
      assertThat(dayModel2.date).isEqualTo(day2)
      assertThat(dayModel2.shiftType).isEqualTo(FullDayActivityType.SHIFT)
      assertThat(dayModel2.details).hasSize(4)
      // 3 significant events but 4 details total
      assertThat(dayModel2.details.filter { it.displayType != null }).hasSize(3)

      val startTask2 = dayModel2.details.first { it.displayType == TaskDisplayType.NIGHT_FINISH }
      assertThat(startTask2.displayTypeTime).isEqualTo(day2.atTime(LocalTime.of(7, 45)))
      assertThat(startTask2.finishDuration).isEqualTo(39600L)

      val overtimeStartTask = dayModel2.details.first { it.displayType == TaskDisplayType.OVERTIME_DAY_START }
      assertThat(overtimeStartTask.displayTypeTime).isEqualTo(day2.atTime(LocalTime.of(17, 15)))
      assertThat(overtimeStartTask.finishDuration).isNull()

      val overtimeEndTask = dayModel2.details.first { it.displayType == TaskDisplayType.OVERTIME_DAY_FINISH }
      assertThat(overtimeEndTask.displayTypeTime).isEqualTo(day2.atTime(LocalTime.of(22, 0)))
      assertThat(overtimeEndTask.finishDuration).isEqualTo(13500L)
    }

    @Test
    fun `Should return a basic night shift with overtime before`() {
      val nightShift = listOf(
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(20, 45)), day2.atTime(LocalTime.of(7, 45)), "Night OSG"),
      )

      val overtime = listOf(
        CsrDetailDto(ShiftType.OVERTIME, day1.atTime(LocalTime.of(17, 15)), day1.atTime(LocalTime.of(18, 30)), "Present"),
        CsrDetailDto(ShiftType.OVERTIME, day1.atTime(LocalTime.of(18, 30)), day1.atTime(LocalTime.of(19, 30)), "Break (Unpaid)"),
        CsrDetailDto(ShiftType.OVERTIME, day1.atTime(LocalTime.of(19, 30)), day1.atTime(LocalTime.of(20, 0)), "Present"),
      )

      whenever(prisonService.getPrisonForUser()).thenReturn(Prison("prison", "", "", 1))
      whenever(csrApiClient.getDetailsForUser(day1, day2, 1, "xyz")).thenReturn(nightShift + overtime)

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day2))

      verify(prisonService).getPrisonForUser()
      verify(csrApiClient).getDetailsForUser(day1, day2, 1, "xyz")

      assertThat(dayModelList).hasSize(2)

      val dayModel1 = dayModelList.first { it.date == day1 }
      assertThat(dayModel1.date).isEqualTo(day1)
      assertThat(dayModel1.shiftType).isEqualTo(FullDayActivityType.SHIFT)
      assertThat(dayModel1.details).hasSize(4)
      // 3 significant events but 4 details total
      assertThat(dayModel1.details.filter { it.displayType != null }).hasSize(3)

      val overtimeStartTask = dayModel1.details.first { it.displayType == TaskDisplayType.OVERTIME_DAY_START }
      assertThat(overtimeStartTask.displayTypeTime).isEqualTo(day1.atTime(LocalTime.of(17, 15)))
      assertThat(overtimeStartTask.finishDuration).isNull()

      val overtimeEndTask = dayModel1.details.first { it.displayType == TaskDisplayType.OVERTIME_DAY_FINISH }
      assertThat(overtimeEndTask.displayTypeTime).isEqualTo(day1.atTime(LocalTime.of(20, 0)))
      assertThat(overtimeEndTask.finishDuration).isEqualTo(6300L)

      val startTask1 = dayModel1.details.first { it.displayType == TaskDisplayType.NIGHT_START }
      assertThat(startTask1.displayTypeTime).isEqualTo(day1.atTime(LocalTime.of(20, 45)))
      assertThat(startTask1.finishDuration).isNull()

      val dayModel2 = dayModelList.first { it.date == day2 }
      assertThat(dayModel2.date).isEqualTo(day2)
      assertThat(dayModel2.shiftType).isEqualTo(FullDayActivityType.SHIFT)
      assertThat(dayModel2.details).hasSize(1)

      val startTask2 = dayModel2.details.first { it.displayType == TaskDisplayType.NIGHT_FINISH }
      assertThat(startTask2.displayTypeTime).isEqualTo(day2.atTime(LocalTime.of(7, 45)))
      assertThat(startTask2.finishDuration).isEqualTo(39600L)
    }
  }
}
