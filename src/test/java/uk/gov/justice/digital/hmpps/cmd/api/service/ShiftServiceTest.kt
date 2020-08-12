package uk.gov.justice.digital.hmpps.cmd.api.service

import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.client.CsrApiClient
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.client.ShiftTaskDto
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.service.ShiftService
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@ExtendWith(MockKExtension::class)
@DisplayName("Shift Service tests")
internal class ShiftServiceTest {
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
        fun `Should default to today if no dates`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    ShiftTaskDto(day1, "Unspecific", day1.atTime(7,15), day1.atTime(12,30), "Present"),
                    ShiftTaskDto(day1, "Break", day1.atTime(12,30), day1.atTime(13,30), "Break (Unpaid)"),
                    ShiftTaskDto(day1, "Unspecific", day1.atTime(13,30), day1.atTime(17,0), "Present")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns dayShift
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns listOf()

            val dayModelList = service.getShiftsBetween(Optional.empty(), Optional.empty())

            verify { csrApiClient.getShiftTasks(day1, day1) }
            verify { csrApiClient.getOvertimeShiftTasks(day1, day1) }

            assertThat(dayModelList).hasSize(1)

        }

        @Test
        fun `Should not modify passed in dates`() {
            val day1 = LocalDate.now(clock)
            val day2 = day1.plusDays(1)
            val dayShift = listOf(
                    ShiftTaskDto(day1, "Unspecific", day1.atTime(7,15), day1.atTime(12,30), "Present"),
                    ShiftTaskDto(day1, "Break", day1.atTime(12,30), day1.atTime(13,30), "Break (Unpaid)"),
                    ShiftTaskDto(day1, "Unspecific", day1.atTime(13,30), day1.atTime(17,0), "Present")
            )

            every { csrApiClient.getShiftTasks(day1, day2) } returns dayShift
            every { csrApiClient.getOvertimeShiftTasks(day1, day2) } returns listOf()

            val dayModelList = service.getShiftsBetween(Optional.of(day1), Optional.of(day2))

            verify { csrApiClient.getShiftTasks(day1, day2) }
            verify { csrApiClient.getOvertimeShiftTasks(day1, day2) }

            assertThat(dayModelList).hasSize(2)

        }

        @Test
        fun `Should return no day for no task data`() {
            val day1 = LocalDate.now(clock)

            every { csrApiClient.getShiftTasks(day1, day1) } returns listOf()
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns listOf()

            val dayModelList = service.getShiftsBetween(Optional.of(day1), Optional.of(day1))

            verify { csrApiClient.getShiftTasks(day1, day1) }
            verify { csrApiClient.getOvertimeShiftTasks(day1, day1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("None")

        }

        @Test
        fun `Should return shift for shift data`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    ShiftTaskDto(day1, "Unspecific", day1.atTime(7,15), day1.atTime(12,30), "Present"),
                    ShiftTaskDto(day1, "Break", day1.atTime(12,30), day1.atTime(13,30), "Break (Unpaid)"),
                    ShiftTaskDto(day1, "Unspecific", day1.atTime(13,30), day1.atTime(17,0), "Present")
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
        fun `Should shift for overtime data`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    ShiftTaskDto(day1, "Unspecific", day1.atTime(7,15), day1.atTime(12,30), "Present"),
                    ShiftTaskDto(day1, "Break", day1.atTime(12,30), day1.atTime(13,30), "Break (Unpaid)"),
                    ShiftTaskDto(day1, "Unspecific", day1.atTime(13,30), day1.atTime(17,0), "Present")
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
        fun `Should not return things for a different dates`() {
            val day1 = LocalDate.now(clock)
            val day2 = day1.plusDays(1)
            val dayShift = listOf(
                    ShiftTaskDto(day1, "Unspecific", day1.atTime(7,15), day1.atTime(12,30), "Present"),
                    ShiftTaskDto(day1, "Break", day1.atTime(12,30), day1.atTime(13,30), "Break (Unpaid)"),
                    ShiftTaskDto(day1, "Unspecific", day1.atTime(13,30), day1.atTime(17,0), "Present")
            )

            every { csrApiClient.getShiftTasks(day2, day2) } returns dayShift
            every { csrApiClient.getOvertimeShiftTasks(day2, day2) } returns listOf()

            val dayModelList = service.getShiftsBetween(Optional.of(day2), Optional.of(day2))

            verify { csrApiClient.getShiftTasks(day2, day2) }
            verify { csrApiClient.getOvertimeShiftTasks(day2, day2) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day2)
            assertThat(dayModel.fullDayType).isEqualTo("None")
            assertThat(dayModel.tasks).hasSize(0)

        }

    }
}