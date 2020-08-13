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
@DisplayName("Shift Service tests")
internal class ShiftServiceTest_Task_Scenarios {
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
                    ShiftTaskDto(day1, "Unspecific", day1.atTime(7, 15), day1.atTime(12, 30), "Present"),
                    ShiftTaskDto(day1, "Break", day1.atTime(12, 30), day1.atTime(13, 30), "Break (Unpaid)"),
                    ShiftTaskDto(day1, "Unspecific", day1.atTime(13, 30), day1.atTime(17, 0), "Present")
            )

            every { csrApiClient.getShiftTasks(day1, day1) } returns dayShift
            every { csrApiClient.getOvertimeShiftTasks(day1, day1) } returns listOf()

            val dayModelList = service.getShiftsBetween(Optional.empty(), Optional.empty())

            verify { csrApiClient.getShiftTasks(day1, day1) }
            verify { csrApiClient.getOvertimeShiftTasks(day1, day1) }

            assertThat(dayModelList).hasSize(1)

        }

    }
}