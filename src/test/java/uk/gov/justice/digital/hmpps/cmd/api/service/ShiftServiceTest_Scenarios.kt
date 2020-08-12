package uk.gov.justice.digital.hmpps.cmd.api.service

import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrApiClient
import uk.gov.justice.digital.hmpps.cmd.api.client.ShiftTaskDto
import uk.gov.justice.digital.hmpps.cmd.api.domain.TaskDisplayType
import uk.gov.justice.digital.hmpps.cmd.api.service.ShiftService
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

@ExtendWith(MockKExtension::class)
@DisplayName("Shift Service tests - real test scenarios")
internal class ShiftServiceTest_Scenarios {
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
        fun `Should return a basic day shift`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    ShiftTaskDto(day1, "Unspecific", day1.atTime(7,15), day1.atTime(12,30), "Present"),
                    ShiftTaskDto(day1, "Break", day1.atTime(12,30), day1.atTime(13,30), "Break (Unpaid)"),
                    ShiftTaskDto(day1, "Unspecific", day1.atTime(13,30), day1.atTime(17,0), "Present")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns dayShift
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns listOf()


            val dayModelList = service.getShiftsBetween(Optional.of(day1), Optional.of(day1))

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("Shift")
            assertThat(dayModel.tasks).hasSize(2)

            val startTask = dayModel.tasks.first{ it.displayType == TaskDisplayType.DAY_START.value}
            assertThat(startTask.eventTime).isEqualTo(LocalTime.of(7,15))
            assertThat(startTask.finishDuration).isNull()

            val endTask = dayModel.tasks.first{ it.displayType == TaskDisplayType.DAY_FINISH.value}
            assertThat(endTask.eventTime).isEqualTo(LocalTime.of(17,0))
            assertThat(endTask.finishDuration).isEqualTo("8h 45m")

        }

        @Test
        fun `Should return a basic night shift`() {
            val day1 = LocalDate.now(clock)
            val day2 = day1.plusDays(1)

            val nightShift = listOf(
                    ShiftTaskDto(day1, "Shift", day1.atTime(20,45), day2.atTime(7,45), "Night OSG")
            )

            every { csrApiClient.getShiftTasks(day1, day2) } returns nightShift
            every { csrApiClient.getOvertimeShiftTasks(day1, day2) } returns listOf()

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
            assertThat(dayModel2.tasks).hasSize(1)

            val startTask2 = dayModel2.tasks.first{ it.displayType == TaskDisplayType.NIGHT_FINISH.value}
            assertThat(startTask2.eventTime).isEqualTo(LocalTime.of(7,45))
            assertThat(startTask2.finishDuration).isEqualTo("11h 00m")
        }

        // The legacy system doesn't show the finish if it landed on a rest day
        @Test
        fun `Should return a basic night shift that finishes on a rest day`() {
            val day1 = LocalDate.now(clock)
            val day2 = day1.plusDays(1)

            val nightShift = listOf(
                    ShiftTaskDto(day1, "Shift", day1.atTime(20,45), day2.atTime(7,45), "Night OSG"),
                    ShiftTaskDto(day2, "Absence", day2.atTime(0,0), day2.atTime(23,59,59), "Rest Day")
            )

            every { csrApiClient.getShiftTasks(day1, day2) } returns nightShift
            every { csrApiClient.getOvertimeShiftTasks(day1, day2) } returns listOf()

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
            assertThat(dayModel2.fullDayType).isEqualTo("Rest Day")
            assertThat(dayModel2.tasks).hasSize(1)

            val startTask2 = dayModel2.tasks.first{ it.displayType == TaskDisplayType.NIGHT_FINISH.value}
            assertThat(startTask2.eventTime).isEqualTo(LocalTime.of(7,45))
            assertThat(startTask2.finishDuration).isEqualTo("11h 00m")
        }

        // The legacy system doesn't show the start if it landed on a rest day
        @Test
        fun `Should return a basic night shift that starts on a rest day`() {
            val day1 = LocalDate.now(clock)
            val day2 = day1.plusDays(1)

            val nightShift = listOf(
                    ShiftTaskDto(day1, "Absence", day1.atTime(0,0), day1.atTime(23,59,59), "Rest Day"),
                    ShiftTaskDto(day1, "Shift", day1.atTime(20,45), day2.atTime(7,45), "Night OSG")
            )

            every { csrApiClient.getShiftTasks(day1, day2) } returns nightShift
            every { csrApiClient.getOvertimeShiftTasks(day1, day2) } returns listOf()

            val dayModelList = service.getShiftsBetween(Optional.of(day1), Optional.of(day2))

            assertThat(dayModelList).hasSize(2)

            val dayModel1 = dayModelList.first{ it.date == day1 }
            assertThat(dayModel1.date).isEqualTo(day1)
            assertThat(dayModel1.fullDayType).isEqualTo("Rest Day")
            assertThat(dayModel1.tasks).hasSize(1)

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

        // The legacy system repeated the last day finish if there was a night shift start on that day.
        @Test
        fun `Should not repeat day shift finsih if night shift starts on that day`() {
            val day1 = LocalDate.now(clock)
            val day2 = day1.plusDays(1)
            val day3 = day2.plusDays(1)

            val nightShift = listOf(
                    ShiftTaskDto(day1, "Unspecific", day1.atTime(7,15), day1.atTime(12,30), "Present"),
                    ShiftTaskDto(day1, "Break", day1.atTime(12,30), day1.atTime(13,30), "Break (Unpaid)"),
                    ShiftTaskDto(day1, "Unspecific", day1.atTime(13,30), day1.atTime(17,0), "Present"),
                    ShiftTaskDto(day2, "Shift", day2.atTime(20,45), day3.atTime(7,45), "Night OSG")
            )

            every { csrApiClient.getShiftTasks(day1, day2) } returns nightShift
            every { csrApiClient.getOvertimeShiftTasks(day1, day2) } returns listOf()

            val dayModelList = service.getShiftsBetween(Optional.of(day1), Optional.of(day2))

            assertThat(dayModelList).hasSize(2)

            val dayModel1 = dayModelList.first{ it.date == day1 }
            assertThat(dayModel1.date).isEqualTo(day1)
            assertThat(dayModel1.fullDayType).isEqualTo("Shift")
            assertThat(dayModel1.tasks).hasSize(2)

            val startTask1 = dayModel1.tasks.first{ it.displayType == TaskDisplayType.DAY_START.value}
            assertThat(startTask1.eventTime).isEqualTo(LocalTime.of(7,15))
            assertThat(startTask1.finishDuration).isNull()

            val endTask1 = dayModel1.tasks.first{ it.displayType == TaskDisplayType.DAY_FINISH.value}
            assertThat(endTask1.eventTime).isEqualTo(LocalTime.of(17,0))
            assertThat(endTask1.finishDuration).isEqualTo("8h 45m")

            val dayModel2 = dayModelList.first{ it.date == day2 }
            assertThat(dayModel2.date).isEqualTo(day2)
            assertThat(dayModel2.fullDayType).isEqualTo("Shift")
            assertThat(dayModel2.tasks).hasSize(1)

            val startTask2 = dayModel2.tasks.first{ it.displayType == TaskDisplayType.NIGHT_START.value}
            assertThat(startTask2.eventTime).isEqualTo(LocalTime.of(20,45))
            assertThat(startTask2.finishDuration).isNull()
        }
    }
}