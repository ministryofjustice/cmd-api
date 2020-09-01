package uk.gov.justice.digital.hmpps.cmd.api.service

import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrClient
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrDetailDto
import uk.gov.justice.digital.hmpps.cmd.api.domain.TaskDisplayType
import uk.gov.justice.digital.hmpps.cmd.api.model.Prison
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.DetailType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.EntityType
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

@ExtendWith(MockKExtension::class)
@DisplayName("Shift Service tests")
internal class ShiftServiceTest_Task {
    private val csrApiClient: CsrClient = mockk(relaxUnitFun = true)
    private val prisonService: PrisonService = mockk(relaxUnitFun = true)
    private val clock = Clock.fixed(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
    private val service = ShiftService(prisonService, csrApiClient, clock)

    @BeforeEach
    fun resetAllMocks() {
        clearMocks(csrApiClient)
        clearMocks(prisonService)
    }

    @AfterEach
    fun confirmVerifiedMocks() {
        confirmVerified(csrApiClient)
        confirmVerified(prisonService)
    }

    @Nested
    @DisplayName("Get Shift tests")
    inner class GetPrisonsTest {

        @Test
        fun `Should default to today if no dates`() {
            val day1 = LocalDate.now(clock)
            val shifts = listOf(
                    CsrDetailDto(day1, EntityType.SHIFT, LocalTime.of(7,15).toSecondOfDay().toLong(), LocalTime.of(12,30).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Present"),
                    CsrDetailDto(day1, EntityType.SHIFT, LocalTime.of(12,30).toSecondOfDay().toLong(), LocalTime.of(13,30).toSecondOfDay().toLong(), DetailType.BREAK, "Break (Unpaid)"),
                    CsrDetailDto(day1, EntityType.SHIFT, LocalTime.of(13,30).toSecondOfDay().toLong(), LocalTime.of(17,0).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Present")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1) } returns shifts

            val dayModel = service.getDetailsForUser(Optional.empty(), Optional.empty())

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1) }

            assertThat(dayModel.first().date).isEqualTo(day1)
        }

        @Test
        fun `Should not modify passed in dates`() {
            val day1 = LocalDate.now(clock)
            val shifts = listOf(
                    CsrDetailDto(day1, EntityType.SHIFT, LocalTime.of(7,15).toSecondOfDay().toLong(), LocalTime.of(12,30).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Present"),
                    CsrDetailDto(day1, EntityType.SHIFT, LocalTime.of(12,30).toSecondOfDay().toLong(), LocalTime.of(13,30).toSecondOfDay().toLong(), DetailType.BREAK, "Break (Unpaid)"),
                    CsrDetailDto(day1, EntityType.SHIFT, LocalTime.of(13,30).toSecondOfDay().toLong(), LocalTime.of(17,0).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Present")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1) } returns shifts
            
            val dayModel = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1) }

            assertThat(dayModel)

        }

        @Test
        fun `Should return 'no day' for no task data`() {
            val day1 = LocalDate.now(clock)

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1) } returns listOf()
            
            val dayModel = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1) }

            assertThat(dayModel.first().date).isEqualTo(day1)
            assertThat(dayModel.first().fullDayType).isEqualTo("None")

        }

        @Test
        fun `Should return 'shift' for shift data`() {
            val day1 = LocalDate.now(clock)
            val shifts = listOf(
                    CsrDetailDto(day1, EntityType.SHIFT, LocalTime.of(7,15).toSecondOfDay().toLong(), LocalTime.of(12,30).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Present"),
                    CsrDetailDto(day1, EntityType.SHIFT, LocalTime.of(12,30).toSecondOfDay().toLong(), LocalTime.of(13,30).toSecondOfDay().toLong(), DetailType.BREAK, "Break (Unpaid)"),
                    CsrDetailDto(day1, EntityType.SHIFT, LocalTime.of(13,30).toSecondOfDay().toLong(), LocalTime.of(17,0).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Present")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1) } returns shifts

            val dayModel = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1) }

            assertThat(dayModel.first().date).isEqualTo(day1)
            assertThat(dayModel.first().fullDayType).isEqualTo("Shift")
        }

        @Test
        fun `Should return 'shift' for overtime data`() {
            val day1 = LocalDate.now(clock)
            val shifts = listOf(
                    CsrDetailDto(day1, EntityType.OVERTIME, LocalTime.of(7,15).toSecondOfDay().toLong(), LocalTime.of(12,30).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Present"),
                    CsrDetailDto(day1, EntityType.OVERTIME, LocalTime.of(12,30).toSecondOfDay().toLong(), LocalTime.of(13,30).toSecondOfDay().toLong(), DetailType.BREAK, "Break (Unpaid)"),
                    CsrDetailDto(day1, EntityType.OVERTIME, LocalTime.of(13,30).toSecondOfDay().toLong(), LocalTime.of(17,0).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Present")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1) } returns shifts

            val dayModel = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1) }

            assertThat(dayModel.first().date).isEqualTo(day1)
            assertThat(dayModel.first().fullDayType).isEqualTo("Shift")
        }

        @Test
        fun `Should not return things for a different dates`() {
            val day1 = LocalDate.now(clock)
            val day2 = day1.plusDays(1)
            val shifts = listOf(
                    CsrDetailDto(day1, EntityType.SHIFT, LocalTime.of(7,15).toSecondOfDay().toLong(), LocalTime.of(12,30).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Present"),
                    CsrDetailDto(day1, EntityType.SHIFT, LocalTime.of(12,30).toSecondOfDay().toLong(), LocalTime.of(13,30).toSecondOfDay().toLong(), DetailType.BREAK, "Break (Unpaid)"),
                    CsrDetailDto(day1, EntityType.SHIFT, LocalTime.of(13,30).toSecondOfDay().toLong(), LocalTime.of(17,0).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Present")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day2, day2, 1) } returns shifts

            val dayModel = service.getDetailsForUser(Optional.of(day2), Optional.of(day2))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day2, day2, 1) }

            assertThat(dayModel.first().date).isEqualTo(day2)
            assertThat(dayModel.first().fullDayType).isEqualTo("None")
            assertThat(dayModel.first().details).hasSize(0)

        }

        @Test
        fun `Should identify Shift start`() {
            val day1 = LocalDate.now(clock)
            val shifts = listOf(
                    CsrDetailDto(day1, EntityType.SHIFT, LocalTime.of(7,15).toSecondOfDay().toLong(), LocalTime.of(12,30).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Present"),
                    CsrDetailDto(day1, EntityType.SHIFT, LocalTime.of(12,30).toSecondOfDay().toLong(), LocalTime.of(13,30).toSecondOfDay().toLong(), DetailType.BREAK, "Break (Unpaid)"),
                    CsrDetailDto(day1, EntityType.SHIFT, LocalTime.of(13,30).toSecondOfDay().toLong(), LocalTime.of(17,0).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Present")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1) } returns shifts

            val dayModel = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1) }

            assertThat(dayModel.first().date).isEqualTo(day1)
            assertThat(dayModel.first().fullDayType).isEqualTo("Shift")

            val overtimeStartTask = dayModel.first().details.first{ it.displayType == TaskDisplayType.DAY_START}
            assertThat(overtimeStartTask.start).isEqualTo(LocalTime.of(7,15))
            assertThat(overtimeStartTask.end).isEqualTo(LocalTime.of(12,30))
            assertThat(overtimeStartTask.eventTime).isEqualTo(LocalTime.of(7,15))
            assertThat(overtimeStartTask.finishDuration).isNull()
        }

        @Test
        fun `Should identify Shift end`() {
            val day1 = LocalDate.now(clock)
            val shifts = listOf(
                    CsrDetailDto(day1, EntityType.SHIFT, LocalTime.of(7,15).toSecondOfDay().toLong(), LocalTime.of(12,30).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Present"),
                    CsrDetailDto(day1, EntityType.SHIFT, LocalTime.of(12,30).toSecondOfDay().toLong(), LocalTime.of(13,30).toSecondOfDay().toLong(), DetailType.BREAK, "Break (Unpaid)"),
                    CsrDetailDto(day1, EntityType.SHIFT, LocalTime.of(13,30).toSecondOfDay().toLong(), LocalTime.of(17,0).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Present")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1) } returns shifts

            val dayModel = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1) }

            assertThat(dayModel.first().date).isEqualTo(day1)
            assertThat(dayModel.first().fullDayType).isEqualTo("Shift")

            val overtimeStartTask = dayModel.first().details.first{ it.displayType == TaskDisplayType.DAY_FINISH}
            assertThat(overtimeStartTask.start).isEqualTo(LocalTime.of(13,30))
            assertThat(overtimeStartTask.end).isEqualTo(LocalTime.of(17,0))
            assertThat(overtimeStartTask.eventTime).isEqualTo(LocalTime.of(17,0))
            assertThat(overtimeStartTask.finishDuration).isEqualTo("8h 45m")
        }

        @Test
        fun `Should identify Night Shift start`() {
            val day1 = LocalDate.now(clock)
            val shifts = listOf(
                    CsrDetailDto(day1, EntityType.SHIFT, LocalTime.of(20,15).toSecondOfDay().toLong(), LocalTime.of(12,30).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Nights OSG")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1) } returns shifts

            val dayModel = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1) }

            assertThat(dayModel.first().date).isEqualTo(day1)
            assertThat(dayModel.first().fullDayType).isEqualTo("Shift")

            val overtimeStartTask = dayModel.first().details.first{ it.displayType == TaskDisplayType.NIGHT_START}
            assertThat(overtimeStartTask.start).isEqualTo(LocalTime.of(20,15))
            assertThat(overtimeStartTask.end).isEqualTo(LocalTime.of(12,30))
            assertThat(overtimeStartTask.eventTime).isEqualTo(LocalTime.of(20,15))

            assertThat(overtimeStartTask.finishDuration).isNull()
        }

        @Test
        fun `Should identify Night Shift end`() {
            val day1 = LocalDate.now(clock)
            val day2 = day1.plusDays(1)
            val shifts = listOf(
                    CsrDetailDto(day1, EntityType.SHIFT, LocalTime.of(20,15).toSecondOfDay().toLong(), LocalTime.of(12,30).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Nights OSG")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day2, day2, 1) } returns shifts

            val dayModel = service.getDetailsForUser(Optional.of(day2), Optional.of(day2))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day2, day2, 1) }

            assertThat(dayModel.first().date).isEqualTo(day2)
            assertThat(dayModel.first().fullDayType).isEqualTo("Shift")

            val overtimeStartTask = dayModel.first().details.first{ it.displayType == TaskDisplayType.NIGHT_FINISH}
            assertThat(overtimeStartTask.start).isEqualTo(LocalTime.of(20,15))
            assertThat(overtimeStartTask.end).isEqualTo(LocalTime.of(12,30))
            assertThat(overtimeStartTask.eventTime).isEqualTo(LocalTime.of(12,30))
            assertThat(overtimeStartTask.finishDuration).isEqualTo("16h 15m")
        }

        @Test
        fun `Should identify Overtime Night Shift start`() {
            val day1 = LocalDate.now(clock)
            val shifts = listOf(
                    CsrDetailDto(day1, EntityType.OVERTIME, LocalTime.of(20,15).toSecondOfDay().toLong(), LocalTime.of(12,30).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Nights OSG")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1) } returns shifts

            val dayModel = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1) }

            assertThat(dayModel.first().date).isEqualTo(day1)
            assertThat(dayModel.first().fullDayType).isEqualTo("Shift")

            val overtimeStartTask = dayModel.first().details.first{ it.displayType == TaskDisplayType.OVERTIME_NIGHT_START}
            assertThat(overtimeStartTask.start).isEqualTo(LocalTime.of(20,15))
            assertThat(overtimeStartTask.end).isEqualTo(LocalTime.of(12,30))
            assertThat(overtimeStartTask.eventTime).isEqualTo(LocalTime.of(20,15))
            assertThat(overtimeStartTask.finishDuration).isNull()
        }

        @Test
        fun `Should identify Overtime Night Shift end`() {
            val day1 = LocalDate.now(clock)
            val day2 = day1.plusDays(1)
            val shifts = listOf(
                    CsrDetailDto(day1, EntityType.OVERTIME, LocalTime.of(20,15).toSecondOfDay().toLong(), LocalTime.of(12,30).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Nights OSG")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day2, day2, 1) } returns shifts

            val dayModel = service.getDetailsForUser(Optional.of(day2), Optional.of(day2))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day2, day2, 1) }

            assertThat(dayModel.first().date).isEqualTo(day2)
            assertThat(dayModel.first().fullDayType).isEqualTo("Shift")

            val overtimeStartTask = dayModel.first().details.first{ it.displayType == TaskDisplayType.OVERTIME_NIGHT_FINISH}
            assertThat(overtimeStartTask.start).isEqualTo(LocalTime.of(20,15))
            assertThat(overtimeStartTask.end).isEqualTo(LocalTime.of(12,30))
            assertThat(overtimeStartTask.eventTime).isEqualTo(LocalTime.of(12,30))
            assertThat(overtimeStartTask.finishDuration).isEqualTo("16h 15m")
        }
    }
} 