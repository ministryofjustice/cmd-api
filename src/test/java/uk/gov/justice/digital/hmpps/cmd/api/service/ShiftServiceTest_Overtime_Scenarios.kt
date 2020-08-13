package uk.gov.justice.digital.hmpps.cmd.api.service

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrApiClient
import uk.gov.justice.digital.hmpps.cmd.api.client.ShiftTaskDto
import uk.gov.justice.digital.hmpps.cmd.api.domain.TaskDisplayType
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

@ExtendWith(MockKExtension::class)
@DisplayName("Shift Service tests with Overtime")
internal class ShiftServiceTest_Overtime_Scenarios {
    private val csrApiClient: CsrApiClient = mockk(relaxUnitFun = true)
    private val clock = Clock.fixed(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
    private val service = ShiftService(csrApiClient, clock)

    @BeforeEach
    fun resetAllMocks() {
        clearMocks(csrApiClient)
    }

    @Nested
    @DisplayName("Get Shift tests")
    inner class GetPrisonsTest {

        @Test
        fun `Should return a basic day shift with overtime after`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    ShiftTaskDto(day1, "Unspecific", day1.atTime(7,15), day1.atTime(12,30), "Present"),
                    ShiftTaskDto(day1, "Break", day1.atTime(12,30), day1.atTime(13,30), "Break (Unpaid)"),
                    ShiftTaskDto(day1, "Unspecific", day1.atTime(13,30), day1.atTime(17,0), "Present")
            )

            val overtime = listOf(
                    ShiftTaskDto(day1, "Unspecific", day1.atTime(17,15), day1.atTime(18,30), "Present"),
                    ShiftTaskDto(day1, "Break", day1.atTime(18,30), day1.atTime(19,30), "Break (Unpaid)"),
                    ShiftTaskDto(day1, "Unspecific", day1.atTime(19,30), day1.atTime(22,0), "Present")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns dayShift
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns overtime


            val dayModelList = service.getShiftsBetween(Optional.of(day1), Optional.of(day1))

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("Shift")
            assertThat(dayModel.tasks).hasSize(4)

            val startTask = dayModel.tasks.first{ it.displayType == TaskDisplayType.DAY_START.value}
            assertThat(startTask.eventTime).isEqualTo(LocalTime.of(7,15))
            assertThat(startTask.finishDuration).isNull()

            val endTask = dayModel.tasks.first{ it.displayType == TaskDisplayType.DAY_FINISH.value}
            assertThat(endTask.eventTime).isEqualTo(LocalTime.of(17,0))
            assertThat(endTask.finishDuration).isEqualTo("8h 45m")

            val overtimeStartTask = dayModel.tasks.first{ it.displayType == TaskDisplayType.OVERTIME_DAY_START.value}
            assertThat(overtimeStartTask.eventTime).isEqualTo(LocalTime.of(17,15))
            assertThat(overtimeStartTask.finishDuration).isNull()

            val overtimeEndTask = dayModel.tasks.first{ it.displayType == TaskDisplayType.OVERTIME_DAY_FINISH.value}
            assertThat(overtimeEndTask.eventTime).isEqualTo(LocalTime.of(22,0))
            assertThat(overtimeEndTask.finishDuration).isEqualTo("3h 45m")
        }

        @Test
        fun `Should return a basic day shift with overtime before`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    ShiftTaskDto(day1, "Unspecific", day1.atTime(17,15), day1.atTime(18,30), "Present"),
                    ShiftTaskDto(day1, "Break", day1.atTime(18,30), day1.atTime(13,30), "Break (Unpaid)"),
                    ShiftTaskDto(day1, "Unspecific", day1.atTime(19,30), day1.atTime(22,0), "Present")
            )

            val overtime = listOf(
                    ShiftTaskDto(day1, "Unspecific", day1.atTime(7,15), day1.atTime(12,30), "Present"),
                    ShiftTaskDto(day1, "Break", day1.atTime(12,30), day1.atTime(13,30), "Break (Unpaid)"),
                    ShiftTaskDto(day1, "Unspecific", day1.atTime(13,30), day1.atTime(17,0), "Present")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns dayShift
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns overtime


            val dayModelList = service.getShiftsBetween(Optional.of(day1), Optional.of(day1))

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("Shift")
            assertThat(dayModel.tasks).hasSize(4)

            val overtimeStartTask = dayModel.tasks.first{ it.displayType == TaskDisplayType.DAY_START.value}
            assertThat(overtimeStartTask.eventTime).isEqualTo(LocalTime.of(17,15))
            assertThat(overtimeStartTask.finishDuration).isNull()

            val overtimeEndTask = dayModel.tasks.first{ it.displayType == TaskDisplayType.DAY_FINISH.value}
            assertThat(overtimeEndTask.eventTime).isEqualTo(LocalTime.of(22,0))
            assertThat(overtimeEndTask.finishDuration).isEqualTo("3h 45m")

            val startTask = dayModel.tasks.first{ it.displayType == TaskDisplayType.OVERTIME_DAY_START.value}
            assertThat(startTask.eventTime).isEqualTo(LocalTime.of(7,15))
            assertThat(startTask.finishDuration).isNull()

            val endTask = dayModel.tasks.first{ it.displayType == TaskDisplayType.OVERTIME_DAY_FINISH.value}
            assertThat(endTask.eventTime).isEqualTo(LocalTime.of(17,0))
            assertThat(endTask.finishDuration).isEqualTo("8h 45m")
        }

        @Test
        fun `Should return a basic night shift with overtime after`() {
            val day1 = LocalDate.now(clock)
            val day2 = day1.plusDays(1)

            val nightShift = listOf(
                    ShiftTaskDto(day1, "Shift", day1.atTime(20,45), day2.atTime(7,45), "Night OSG")
            )

            val overtime = listOf(
                    ShiftTaskDto(day2, "Unspecific", day2.atTime(17,15), day2.atTime(18,30), "Present"),
                    ShiftTaskDto(day2, "Break", day2.atTime(18,30), day2.atTime(19,30), "Break (Unpaid)"),
                    ShiftTaskDto(day2, "Unspecific", day2.atTime(19,30), day2.atTime(22,0), "Present")
            )

            every { csrApiClient.getShiftTasks(day1, day2) } returns nightShift
            every { csrApiClient.getOvertimeShiftTasks(day1, day2) } returns overtime

            val dayModelList = service.getShiftsBetween(Optional.of(day1), Optional.of(day2))

            assertThat(dayModelList).hasSize(2)

            val dayModel1 = dayModelList.first{ it.date == day1 }
            assertThat(dayModel1.date).isEqualTo(day1)
            assertThat(dayModel1.fullDayType).isEqualTo("Shift")
            assertThat(dayModel1.tasks).hasSize(1)

            val startTask1 = dayModel1.tasks.first{ it.displayType == TaskDisplayType.NIGHT_START.value}
            assertThat(startTask1.eventTime).isEqualTo(LocalTime.of(20,45))
            assertThat(startTask1.finishDuration).isNull()

            val dayModel2 = dayModelList.first{ it.date == day2 }
            assertThat(dayModel2.date).isEqualTo(day2)
            assertThat(dayModel2.fullDayType).isEqualTo("Shift")
            assertThat(dayModel2.tasks).hasSize(3)

            val startTask2 = dayModel2.tasks.first{ it.displayType == TaskDisplayType.NIGHT_FINISH.value}
            assertThat(startTask2.eventTime).isEqualTo(LocalTime.of(7,45))
            assertThat(startTask2.finishDuration).isEqualTo("11h 00m")

            val overtimeStartTask = dayModel2.tasks.first{ it.displayType == TaskDisplayType.OVERTIME_DAY_START.value}
            assertThat(overtimeStartTask.eventTime).isEqualTo(LocalTime.of(17,15))
            assertThat(overtimeStartTask.finishDuration).isNull()

            val overtimeEndTask = dayModel2.tasks.first{ it.displayType == TaskDisplayType.OVERTIME_DAY_FINISH.value}
            assertThat(overtimeEndTask.eventTime).isEqualTo(LocalTime.of(22,0))
            assertThat(overtimeEndTask.finishDuration).isEqualTo("3h 45m")
        }

        @Test
        fun `Should return a basic night shift with overtime before`() {
            val day1 = LocalDate.now(clock)
            val day2 = day1.plusDays(1)

            val nightShift = listOf(
                    ShiftTaskDto(day1, "Shift", day1.atTime(20,45), day2.atTime(7,45), "Night OSG")
            )

            val overtime = listOf(
                    ShiftTaskDto(day1, "Unspecific", day1.atTime(17,15), day1.atTime(18,30), "Present"),
                    ShiftTaskDto(day1, "Break", day1.atTime(18,30), day1.atTime(19,30), "Break (Unpaid)"),
                    ShiftTaskDto(day1, "Unspecific", day1.atTime(19,30), day1.atTime(20,0), "Present")
            )

            every { csrApiClient.getShiftTasks(day1, day2) } returns nightShift
            every { csrApiClient.getOvertimeShiftTasks(day1, day2) } returns overtime

            val dayModelList = service.getShiftsBetween(Optional.of(day1), Optional.of(day2))

            assertThat(dayModelList).hasSize(2)

            val dayModel1 = dayModelList.first{ it.date == day1 }
            assertThat(dayModel1.date).isEqualTo(day1)
            assertThat(dayModel1.fullDayType).isEqualTo("Shift")
            assertThat(dayModel1.tasks).hasSize(3)

            val overtimeStartTask = dayModel1.tasks.first{ it.displayType == TaskDisplayType.OVERTIME_DAY_START.value}
            assertThat(overtimeStartTask.eventTime).isEqualTo(LocalTime.of(17,15))
            assertThat(overtimeStartTask.finishDuration).isNull()

            val overtimeEndTask = dayModel1.tasks.first{ it.displayType == TaskDisplayType.OVERTIME_DAY_FINISH.value}
            assertThat(overtimeEndTask.eventTime).isEqualTo(LocalTime.of(20,0))
            assertThat(overtimeEndTask.finishDuration).isEqualTo("1h 45m")

            val startTask1 = dayModel1.tasks.first{ it.displayType == TaskDisplayType.NIGHT_START.value}
            assertThat(startTask1.eventTime).isEqualTo(LocalTime.of(20,45))
            assertThat(startTask1.finishDuration).isNull()

            val dayModel2 = dayModelList.first{ it.date == day2 }
            assertThat(dayModel2.date).isEqualTo(day2)
            assertThat(dayModel2.fullDayType).isEqualTo("Shift")
            assertThat(dayModel2.tasks).hasSize(1)

            val startTask2 = dayModel2.tasks.first{ it.displayType == TaskDisplayType.NIGHT_FINISH.value}
            assertThat(startTask2.eventTime).isEqualTo(LocalTime.of(7,45))
            assertThat(startTask2.finishDuration).isEqualTo("11h 00m")
        }
    }
} 