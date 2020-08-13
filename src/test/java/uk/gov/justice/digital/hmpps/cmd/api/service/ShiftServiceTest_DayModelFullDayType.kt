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
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrApiClient
import uk.gov.justice.digital.hmpps.cmd.api.client.ShiftTaskDto
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@ExtendWith(MockKExtension::class)
@DisplayName("Shift Service Full Day Type tests")
internal class ShiftServiceTest_DayModelFullDayType {
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
        fun `Should return activity as Full Day Type for full day Unspecific`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    ShiftTaskDto(CsrApiClient.day1, "Unspecific", day1.atTime(0,0), day1.atTime(23,59,59), "My Activity")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns dayShift
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns listOf()

            val dayModelList = service.getShiftsBetween(Optional.of(day1), Optional.of(day1))

            verify { csrApiClient.getShiftTasks(day1, day1) }
            verify { csrApiClient.getOvertimeShiftTasks(day1, day1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("My Activity")
        }

        @Test
        fun `Should return type as Full Day Type for Training Internal`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    ShiftTaskDto(CsrApiClient.day1, "Shift", day1.atTime(7,15), day1.atTime(12,30), "Training - Internal")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns dayShift
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns listOf()

            val dayModelList = service.getShiftsBetween(Optional.of(day1), Optional.of(day1))

            verify { csrApiClient.getShiftTasks(day1, day1) }
            verify { csrApiClient.getOvertimeShiftTasks(day1, day1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("Training - Internal")
        }

        @Test
        fun `Should return type as Full Day Type for Training external`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    ShiftTaskDto(CsrApiClient.day1, "Shift", day1.atTime(7,15), day1.atTime(12,30), "Training - External")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns dayShift
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns listOf()

            val dayModelList = service.getShiftsBetween(Optional.of(day1), Optional.of(day1))

            verify { csrApiClient.getShiftTasks(day1, day1) }
            verify { csrApiClient.getOvertimeShiftTasks(day1, day1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("Training - External")
        }

        @Test
        fun `Should return activity as Full Day Type for Absence`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    ShiftTaskDto(CsrApiClient.day1, "Absence", day1.atTime(0,0), day1.atTime(23,59,59), "My Activity")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns dayShift
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns listOf()

            val dayModelList = service.getShiftsBetween(Optional.of(day1), Optional.of(day1))

            verify { csrApiClient.getShiftTasks(day1, day1) }
            verify { csrApiClient.getOvertimeShiftTasks(day1, day1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("My Activity")
        }

        @Test
        fun `Should return Holiday as Full Day Type for full day Holiday`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    ShiftTaskDto(CsrApiClient.day1, "Holiday", day1.atTime(0,0), day1.atTime(23,59,59), "My Activity")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns dayShift
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns listOf()

            val dayModelList = service.getShiftsBetween(Optional.of(day1), Optional.of(day1))

            verify { csrApiClient.getShiftTasks(day1, day1) }
            verify { csrApiClient.getOvertimeShiftTasks(day1, day1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("Holiday")
        }

        @Test
        fun `Should return Holiday as Full Day Type for Holiday with not other Unspecific tasks`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    ShiftTaskDto(CsrApiClient.day1, "Holiday", day1.atTime(7,0), day1.atTime(10,0), "My Activity")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns dayShift
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns listOf()

            val dayModelList = service.getShiftsBetween(Optional.of(day1), Optional.of(day1))

            verify { csrApiClient.getShiftTasks(day1, day1) }
            verify { csrApiClient.getOvertimeShiftTasks(day1, day1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("Holiday")
        }

        @Test
        fun `Should return Shift as Full Day Type for Holiday with other Unspecific tasks`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    ShiftTaskDto(CsrApiClient.day1, "Holiday", day1.atTime(7,0), day1.atTime(10,0), "My Activity"),
                    ShiftTaskDto(CsrApiClient.day1, "Unspecific", day1.atTime(19,0), day1.atTime(20,0), "Door Guard")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns dayShift
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns listOf()

            val dayModelList = service.getShiftsBetween(Optional.of(day1), Optional.of(day1))

            verify { csrApiClient.getShiftTasks(day1, day1) }
            verify { csrApiClient.getOvertimeShiftTasks(day1, day1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("Shift")
        }

        @Test
        fun `Should return Illness as Full Day Type for Illness`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    ShiftTaskDto(CsrApiClient.day1, "Illness", day1.atTime(7,0), day1.atTime(10,0), "My Activity")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns dayShift
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns listOf()

            val dayModelList = service.getShiftsBetween(Optional.of(day1), Optional.of(day1))

            verify { csrApiClient.getShiftTasks(day1, day1) }
            verify { csrApiClient.getOvertimeShiftTasks(day1, day1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("Illness")
        }

        @Test
        fun `Should return Shift as Full Day Type if no rules met`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    ShiftTaskDto(CsrApiClient.day1, "New Type", day1.atTime(7,0), day1.atTime(10,0), "My Activity")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns dayShift
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns listOf()

            val dayModelList = service.getShiftsBetween(Optional.of(day1), Optional.of(day1))

            verify { csrApiClient.getShiftTasks(day1, day1) }
            verify { csrApiClient.getOvertimeShiftTasks(day1, day1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("Shift")
        }

        @Test
        fun `Overtime - Should return activity as Full Day Type for full day Unspecific`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    ShiftTaskDto(CsrApiClient.day1, "Unspecific", day1.atTime(0,0), day1.atTime(23,59,59), "My Activity")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns listOf()
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns dayShift

            val dayModelList = service.getShiftsBetween(Optional.of(day1), Optional.of(day1))

            verify { csrApiClient.getShiftTasks(day1, day1) }
            verify { csrApiClient.getOvertimeShiftTasks(day1, day1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("My Activity")
        }

        @Test
        fun `Overtime - Should return type as Full Day Type for Training Internal`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    ShiftTaskDto(CsrApiClient.day1, "Shift", day1.atTime(7,15), day1.atTime(12,30), "Training - Internal")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns listOf()
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns dayShift

            val dayModelList = service.getShiftsBetween(Optional.of(day1), Optional.of(day1))

            verify { csrApiClient.getShiftTasks(day1, day1) }
            verify { csrApiClient.getOvertimeShiftTasks(day1, day1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("Training - Internal")
        }

        @Test
        fun `Overtime Should return type as Full Day Type for Training external`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    ShiftTaskDto(CsrApiClient.day1, "Shift", day1.atTime(7,15), day1.atTime(12,30), "Training - External")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns listOf()
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns dayShift

            val dayModelList = service.getShiftsBetween(Optional.of(day1), Optional.of(day1))

            verify { csrApiClient.getShiftTasks(day1, day1) }
            verify { csrApiClient.getOvertimeShiftTasks(day1, day1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("Training - External")
        }

        @Test
        fun `Overtime - Should return activity as Full Day Type for Absence`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    ShiftTaskDto(CsrApiClient.day1, "Absence", day1.atTime(0,0), day1.atTime(23,59,59), "My Activity")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns listOf()
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns dayShift

            val dayModelList = service.getShiftsBetween(Optional.of(day1), Optional.of(day1))

            verify { csrApiClient.getShiftTasks(day1, day1) }
            verify { csrApiClient.getOvertimeShiftTasks(day1, day1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("My Activity")
        }

        @Test
        fun `Overtime - Should return Holiday as Full Day Type for full day Holiday`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    ShiftTaskDto(CsrApiClient.day1, "Holiday", day1.atTime(0,0), day1.atTime(23,59,59), "My Activity")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns listOf()
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns dayShift

            val dayModelList = service.getShiftsBetween(Optional.of(day1), Optional.of(day1))

            verify { csrApiClient.getShiftTasks(day1, day1) }
            verify { csrApiClient.getOvertimeShiftTasks(day1, day1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("Holiday")
        }

        @Test
        fun `Overtime - Should return Holiday as Full Day Type for Holiday with not other Unspecific tasks`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    ShiftTaskDto(CsrApiClient.day1, "Holiday", day1.atTime(7,0), day1.atTime(10,0), "My Activity")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns listOf()
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns dayShift

            val dayModelList = service.getShiftsBetween(Optional.of(day1), Optional.of(day1))

            verify { csrApiClient.getShiftTasks(day1, day1) }
            verify { csrApiClient.getOvertimeShiftTasks(day1, day1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("Holiday")
        }

        @Test
        fun `Overtime - Should return Shift as Full Day Type for Holiday with other Unspecific tasks`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    ShiftTaskDto(CsrApiClient.day1, "Holiday", day1.atTime(7,0), day1.atTime(10,0), "My Activity"),
                    ShiftTaskDto(CsrApiClient.day1, "Unspecific", day1.atTime(19,0), day1.atTime(20,0), "Door Guard")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns listOf()
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns dayShift

            val dayModelList = service.getShiftsBetween(Optional.of(day1), Optional.of(day1))

            verify { csrApiClient.getShiftTasks(day1, day1) }
            verify { csrApiClient.getOvertimeShiftTasks(day1, day1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("Shift")
        }

        @Test
        fun `Overtime - Should return Illness as Full Day Type for Illness`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    ShiftTaskDto(CsrApiClient.day1, "Illness", day1.atTime(7,0), day1.atTime(10,0), "My Activity")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns listOf()
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns dayShift

            val dayModelList = service.getShiftsBetween(Optional.of(day1), Optional.of(day1))

            verify { csrApiClient.getShiftTasks(day1, day1) }
            verify { csrApiClient.getOvertimeShiftTasks(day1, day1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("Illness")
        }

        @Test
        fun `Overtime - Should return Shift as Full Day Type if no rules met`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    ShiftTaskDto(CsrApiClient.day1, "New Type", day1.atTime(7,0), day1.atTime(10,0), "My Activity")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns listOf()
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns dayShift

            val dayModelList = service.getShiftsBetween(Optional.of(day1), Optional.of(day1))

            verify { csrApiClient.getShiftTasks(day1, day1) }
            verify { csrApiClient.getOvertimeShiftTasks(day1, day1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("Shift")
        }

    }
}