package uk.gov.justice.digital.hmpps.cmd.api.service

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.cmd.api.domain.TaskDisplayType
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrClient
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrDetailDto
import uk.gov.justice.digital.hmpps.cmd.api.client.Elite2ApiClient
import uk.gov.justice.digital.hmpps.cmd.api.model.Prison
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

@ExtendWith(MockKExtension::class)
@DisplayName("Shift Service tests")
internal class ShiftServiceTest {
    private val csrApiClient: CsrClient = mockk(relaxUnitFun = true)
    private val elite2ApiClient: Elite2ApiClient = mockk(relaxUnitFun = true)
    private val prisonService: PrisonService = mockk(relaxUnitFun = true)
    private val clock = Clock.fixed(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
    private val service = ShiftService(prisonService, csrApiClient, elite2ApiClient, clock)

    @BeforeEach
    fun resetAllMocks() {
        clearMocks(csrApiClient)
    }

    @Nested
    @DisplayName("Get Shift tests")
    inner class GetPrisonsTest {

        @Test
        fun `Should default to today if no dates`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    CsrDetailDto(day1, "Unspecific", LocalTime.of(7,15).toSecondOfDay().toLong(), LocalTime.of(12,30).toSecondOfDay().toLong(), "Present"),
                    CsrDetailDto(day1, "Break", LocalTime.of(12,30).toSecondOfDay().toLong(), LocalTime.of(13,30).toSecondOfDay().toLong(), "Break (Unpaid)"),
                    CsrDetailDto(day1, "Unspecific", LocalTime.of(13,30).toSecondOfDay().toLong(), LocalTime.of(17,0).toSecondOfDay().toLong(), "Present")
            )

            every { elite2ApiClient.getCurrentPrisonIdForUser()} returns "prison"
            every { prisonService.getPrisonByPrisonId("prison")} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1) } returns dayShift

            val dayModelList = service.getShiftsForUserBetween(Optional.empty(), Optional.empty())

            verify { elite2ApiClient.getCurrentPrisonIdForUser()}
            verify { prisonService.getPrisonByPrisonId("prison")}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1) }

            assertThat(dayModelList).hasSize(1)

        }

        @Test
        fun `Should not modify passed in dates`() {
            val day1 = LocalDate.now(clock)
            val day2 = day1.plusDays(1)
            val dayShift = listOf(
                    CsrDetailDto(day1, "Unspecific", LocalTime.of(7,15).toSecondOfDay().toLong(), LocalTime.of(12,30).toSecondOfDay().toLong(), "Present"),
                    CsrDetailDto(day1, "Break", LocalTime.of(12,30).toSecondOfDay().toLong(), LocalTime.of(13,30).toSecondOfDay().toLong(), "Break (Unpaid)"),
                    CsrDetailDto(day1, "Unspecific", LocalTime.of(13,30).toSecondOfDay().toLong(), LocalTime.of(17,0).toSecondOfDay().toLong(), "Present")
            )

            every { elite2ApiClient.getCurrentPrisonIdForUser()} returns "prison"
            every { prisonService.getPrisonByPrisonId("prison")} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1) } returns dayShift

            val dayModelList = service.getShiftsForUserBetween(Optional.of(day1), Optional.of(day2))

            verify { elite2ApiClient.getCurrentPrisonIdForUser()}
            verify { prisonService.getPrisonByPrisonId("prison")}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1) }

            assertThat(dayModelList).hasSize(2)

        }

        @Test
        fun `Should return 'no day' for no task data`() {
            val day1 = LocalDate.now(clock)

            every { csrApiClient.getShiftTasks(day1, day1) } returns listOf()
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns listOf()

            val dayModelList = service.getShiftsForUserBetween(Optional.of(day1), Optional.of(day1))

            verify { elite2ApiClient.getCurrentPrisonIdForUser()}
            verify { prisonService.getPrisonByPrisonId("prison")}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("None")

        }

        @Test
        fun `Should return 'shift' for shift data`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    CsrDetailDto(day1, "Unspecific", LocalTime.of(7,15).toSecondOfDay().toLong(), LocalTime.of(12,30).toSecondOfDay().toLong(), "Present"),
                    CsrDetailDto(day1, "Break", LocalTime.of(12,30).toSecondOfDay().toLong(), LocalTime.of(13,30).toSecondOfDay().toLong(), "Break (Unpaid)"),
                    CsrDetailDto(day1, "Unspecific", LocalTime.of(13,30).toSecondOfDay().toLong(), LocalTime.of(17,0).toSecondOfDay().toLong(), "Present")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns listOf()
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns dayShift

            val dayModelList = service.getShiftsForUserBetween(Optional.of(day1), Optional.of(day1))

            verify { elite2ApiClient.getCurrentPrisonIdForUser()}
            verify { prisonService.getPrisonByPrisonId("prison")}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("Shift")
        }

        @Test
        fun `Should return 'shift' for overtime data`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    CsrDetailDto(day1, "Unspecific", LocalTime.of(7,15).toSecondOfDay().toLong(), LocalTime.of(12,30).toSecondOfDay().toLong(), "Present"),
                    CsrDetailDto(day1, "Break", LocalTime.of(12,30).toSecondOfDay().toLong(), LocalTime.of(13,30).toSecondOfDay().toLong(), "Break (Unpaid)"),
                    CsrDetailDto(day1, "Unspecific", LocalTime.of(13,30).toSecondOfDay().toLong(), LocalTime.of(17,0).toSecondOfDay().toLong(), "Present")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns listOf()
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns dayShift

            val dayModelList = service.getShiftsForUserBetween(Optional.of(day1), Optional.of(day1))

            verify { elite2ApiClient.getCurrentPrisonIdForUser()}
            verify { prisonService.getPrisonByPrisonId("prison")}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("Shift")
        }

        @Test
        fun `Should not return things for a different dates`() {
            val day1 = LocalDate.now(clock)
            val day2 = day1.plusDays(1)
            val dayShift = listOf(
                    CsrDetailDto(day1, "Unspecific", LocalTime.of(7,15).toSecondOfDay().toLong(), LocalTime.of(12,30).toSecondOfDay().toLong(), "Present"),
                    CsrDetailDto(day1, "Break", LocalTime.of(12,30).toSecondOfDay().toLong(), LocalTime.of(13,30).toSecondOfDay().toLong(), "Break (Unpaid)"),
                    CsrDetailDto(day1, "Unspecific", LocalTime.of(13,30).toSecondOfDay().toLong(), LocalTime.of(17,0).toSecondOfDay().toLong(), "Present")
            )

            every { csrApiClient.getShiftTasks(day2, day2) } returns dayShift
            every { csrApiClient.getOvertimeShiftTasks(day2, day2) } returns listOf()

            val dayModelList = service.getShiftsForUserBetween(Optional.of(day2), Optional.of(day2))

            verify { elite2ApiClient.getCurrentPrisonIdForUser()}
            verify { prisonService.getPrisonByPrisonId("prison")}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day2)
            assertThat(dayModel.fullDayType).isEqualTo("None")
            assertThat(dayModel.tasks).hasSize(0)

        }

        @Test
        fun `Should identify Shift start`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    CsrDetailDto(day1, "Unspecific", LocalTime.of(7,15).toSecondOfDay().toLong(), LocalTime.of(12,30).toSecondOfDay().toLong(), "Present"),
                    CsrDetailDto(day1, "Break", LocalTime.of(12,30).toSecondOfDay().toLong(), LocalTime.of(13,30).toSecondOfDay().toLong(), "Break (Unpaid)"),
                    CsrDetailDto(day1, "Unspecific", LocalTime.of(13,30).toSecondOfDay().toLong(), LocalTime.of(17,0).toSecondOfDay().toLong(), "Present")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns dayShift
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns listOf()

            val dayModelList = service.getShiftsForUserBetween(Optional.of(day1), Optional.of(day1))

            verify { elite2ApiClient.getCurrentPrisonIdForUser()}
            verify { prisonService.getPrisonByPrisonId("prison")}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("Shift")

            val overtimeStartTask = dayModel.tasks.first{ it.displayType == TaskDisplayType.DAY_START.value}
            assertThat(overtimeStartTask.eventTime).isEqualTo(LocalTime.of(7,15))
            assertThat(overtimeStartTask.finishDuration).isNull()
        }

        @Test
        fun `Should identify Shift end`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    CsrDetailDto(day1, "Unspecific", LocalTime.of(7,15).toSecondOfDay().toLong(), LocalTime.of(12,30).toSecondOfDay().toLong(), "Present"),
                    CsrDetailDto(day1, "Break", LocalTime.of(12,30).toSecondOfDay().toLong(), LocalTime.of(13,30).toSecondOfDay().toLong(), "Break (Unpaid)"),
                    CsrDetailDto(day1, "Unspecific", LocalTime.of(13,30).toSecondOfDay().toLong(), LocalTime.of(17,0).toSecondOfDay().toLong(), "Present")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns dayShift
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns listOf()

            val dayModelList = service.getShiftsForUserBetween(Optional.of(day1), Optional.of(day1))

            verify { elite2ApiClient.getCurrentPrisonIdForUser()}
            verify { prisonService.getPrisonByPrisonId("prison")}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("Shift")

            val overtimeStartTask = dayModel.tasks.first{ it.displayType == TaskDisplayType.DAY_FINISH.value}
            assertThat(overtimeStartTask.eventTime).isEqualTo(LocalTime.of(17,0))
            assertThat(overtimeStartTask.finishDuration).isEqualTo("8h 45m")
        }

        @Test
        fun `Should identify Overtime Shift start`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    CsrDetailDto(day1, "Unspecific", LocalTime.of(7,15).toSecondOfDay().toLong(), LocalTime.of(12,30).toSecondOfDay().toLong(), "Present"),
                    CsrDetailDto(day1, "Break", LocalTime.of(12,30).toSecondOfDay().toLong(), LocalTime.of(13,30).toSecondOfDay().toLong(), "Break (Unpaid)"),
                    CsrDetailDto(day1, "Unspecific", LocalTime.of(13,30).toSecondOfDay().toLong(), LocalTime.of(17,0).toSecondOfDay().toLong(), "Present")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns listOf()
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns dayShift

            val dayModelList = service.getShiftsForUserBetween(Optional.of(day1), Optional.of(day1))

            verify { elite2ApiClient.getCurrentPrisonIdForUser()}
            verify { prisonService.getPrisonByPrisonId("prison")}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("Shift")

            val overtimeStartTask = dayModel.tasks.first{ it.displayType == TaskDisplayType.OVERTIME_DAY_START.value}
            assertThat(overtimeStartTask.eventTime).isEqualTo(LocalTime.of(7,15))
            assertThat(overtimeStartTask.finishDuration).isNull()
        }

        @Test
        fun `Should identify Overtime Shift end`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    CsrDetailDto(day1, "Unspecific", LocalTime.of(7,15).toSecondOfDay().toLong(), LocalTime.of(12,30).toSecondOfDay().toLong(), "Present"),
                    CsrDetailDto(day1, "Break", LocalTime.of(12,30).toSecondOfDay().toLong(), LocalTime.of(13,30).toSecondOfDay().toLong(), "Break (Unpaid)"),
                    CsrDetailDto(day1, "Unspecific", LocalTime.of(13,30).toSecondOfDay().toLong(), LocalTime.of(17,0).toSecondOfDay().toLong(), "Present")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns listOf()
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns dayShift

            val dayModelList = service.getShiftsForUserBetween(Optional.of(day1), Optional.of(day1))

            verify { elite2ApiClient.getCurrentPrisonIdForUser()}
            verify { prisonService.getPrisonByPrisonId("prison")}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("Shift")

            val overtimeStartTask = dayModel.tasks.first{ it.displayType == TaskDisplayType.OVERTIME_DAY_FINISH.value}
            assertThat(overtimeStartTask.eventTime).isEqualTo(LocalTime.of(17,0))
            assertThat(overtimeStartTask.finishDuration).isEqualTo("8h 45m")
        }

        @Test
        fun `Should identify Night Shift start`() {
            val day1 = LocalDate.now(clock)
            val day2 = day1.plusDays(1)
            val nightShift = listOf(
                    CsrDetailDto(day1, "Unspecific", day1.atTime(20,15), day2.atTime(12,30), "Night OSG")
            )

            every { csrApiClient.getShiftTasks(day1, day2) } returns nightShift
            every { csrApiClient.getOvertimeShiftTasks(day1, day2) } returns listOf()

            val dayModelList = service.getShiftsForUserBetween(Optional.of(day1), Optional.of(day2))

            verify { elite2ApiClient.getCurrentPrisonIdForUser()}
            verify { prisonService.getPrisonByPrisonId("prison")}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1) }

            assertThat(dayModelList).hasSize(2)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("Shift")

            val overtimeStartTask = dayModel.tasks.first{ it.displayType == TaskDisplayType.NIGHT_START.value}
            assertThat(overtimeStartTask.eventTime).isEqualTo(LocalTime.of(20,15))
            assertThat(overtimeStartTask.finishDuration).isNull()
        }

        @Test
        fun `Should identify Night Shift end`() {
            val day1 = LocalDate.now(clock)
            val day2 = day1.plusDays(1)
            val nightShift = listOf(
                    CsrDetailDto(day1, "Unspecific", day1.atTime(20,15), day2.atTime(12,30), "Night OSG")
            )

            every { csrApiClient.getShiftTasks(day1, day2) } returns nightShift
            every { csrApiClient.getOvertimeShiftTasks(day1, day2) } returns listOf()

            val dayModelList = service.getShiftsForUserBetween(Optional.of(day1), Optional.of(day2))

            verify { elite2ApiClient.getCurrentPrisonIdForUser()}
            verify { prisonService.getPrisonByPrisonId("prison")}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1) }

            assertThat(dayModelList).hasSize(2)

            val dayModel = dayModelList.last()
            assertThat(dayModel.date).isEqualTo(day2)
            assertThat(dayModel.fullDayType).isEqualTo("Shift")

            val overtimeStartTask = dayModel.tasks.first{ it.displayType == TaskDisplayType.NIGHT_FINISH.value}
            assertThat(overtimeStartTask.eventTime).isEqualTo(LocalTime.of(12,30))
            assertThat(overtimeStartTask.finishDuration).isEqualTo("16h 15m")
        }

        @Test
        fun `Should identify Overtime Night Shift start`() {
            val day1 = LocalDate.now(clock)
            val day2 = day1.plusDays(1)
            val nightShift = listOf(
                    CsrDetailDto(day1, "Unspecific", day1.atTime(20,15), day2.atTime(12,30), "Night OSG")
            )

            every { csrApiClient.getShiftTasks(day1, day2) } returns listOf()
            every { csrApiClient.getOvertimeShiftTasks(day1, day2) } returns nightShift

            val dayModelList = service.getShiftsForUserBetween(Optional.of(day1), Optional.of(day2))

            verify { elite2ApiClient.getCurrentPrisonIdForUser()}
            verify { prisonService.getPrisonByPrisonId("prison")}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1) }

            assertThat(dayModelList).hasSize(2)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("Shift")

            val overtimeStartTask = dayModel.tasks.first{ it.displayType == TaskDisplayType.OVERTIME_NIGHT_START.value}
            assertThat(overtimeStartTask.eventTime).isEqualTo(LocalTime.of(20,15))
            assertThat(overtimeStartTask.finishDuration).isNull()
        }

        @Test
        fun `Should identify Overtime Night Shift end`() {
            val day1 = LocalDate.now(clock)
            val day2 = day1.plusDays(1)
            val nightShift = listOf(
                    CsrDetailDto(day1, "Unspecific", day1.atTime(20,15), day2.atTime(12,30), "Night OSG")
            )

            every { csrApiClient.getShiftTasks(day1, day2) } returns listOf()
            every { csrApiClient.getOvertimeShiftTasks(day1, day2) } returns nightShift

            val dayModelList = service.getShiftsForUserBetween(Optional.of(day1), Optional.of(day2))

            verify { elite2ApiClient.getCurrentPrisonIdForUser()}
            verify { prisonService.getPrisonByPrisonId("prison")}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1) }

            assertThat(dayModelList).hasSize(2)

            val dayModel = dayModelList.last()
            assertThat(dayModel.date).isEqualTo(day2)
            assertThat(dayModel.fullDayType).isEqualTo("Shift")

            val overtimeStartTask = dayModel.tasks.first{ it.displayType == TaskDisplayType.OVERTIME_NIGHT_FINISH.value}
            assertThat(overtimeStartTask.eventTime).isEqualTo(LocalTime.of(12,30))
            assertThat(overtimeStartTask.finishDuration).isEqualTo("16h 15m")
        }
    }
}