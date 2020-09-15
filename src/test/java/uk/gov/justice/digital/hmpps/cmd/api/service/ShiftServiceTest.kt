package uk.gov.justice.digital.hmpps.cmd.api.service

import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrClient
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrDetailDto
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailDisplayType
import uk.gov.justice.digital.hmpps.cmd.api.model.Prison
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.DetailParentType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.DetailType
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

@ExtendWith(MockKExtension::class)
@DisplayName("Shift Service tests")
internal class ShiftServiceTest {
    private val csrApiClient: CsrClient = mockk(relaxUnitFun = true)
    private val prisonService: PrisonService = mockk(relaxUnitFun = true)
    private val authenticationFacade: AuthenticationFacade = mockk(relaxUnitFun = true)
    private val clock = Clock.fixed(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
    private val service = ShiftService(prisonService, csrApiClient, clock, authenticationFacade)

    private val day1 = LocalDate.now(clock)

    @BeforeEach
    fun resetAllMocks() {
        clearMocks(csrApiClient)
        clearMocks(prisonService)

        every { authenticationFacade.currentUsername } returns "xyz"
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
            
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(7,15)), day1.atTime(LocalTime.of(12,30)), "Present"),
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(12,30)), day1.atTime(LocalTime.of(13,30)), "Break (Unpaid)"),
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(13,30)), day1.atTime(LocalTime.of(17,0)), "Present")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.empty(), Optional.empty())

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

            assertThat(dayModelList).hasSize(1)

        }

        @Test
        fun `Should not modify passed in dates`() {
            
            val day2 = day1.plusDays(1)
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(7,15)), day1.atTime(LocalTime.of(12,30)), "Present"),
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(12,30)), day1.atTime(LocalTime.of(13,30)), "Break (Unpaid)"),
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(13,30)), day1.atTime(LocalTime.of(17,0)), "Present")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day2, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day2))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day2, 1, "xyz") }

            assertThat(dayModelList).hasSize(2)

        }

        @Test
        fun `Should return 'no day' for no task data`() {
            

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns listOf()

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.shiftType).isEqualTo(DetailType.NONE)

        }

        @Test
        fun `Should return 'shift' for shift data`() {
            
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(7,15)), day1.atTime(LocalTime.of(12,30)), "Present"),
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(12,30)), day1.atTime(LocalTime.of(13,30)), "Break (Unpaid)"),
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(13,30)), day1.atTime(LocalTime.of(17,0)), "Present")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.shiftType).isEqualTo(DetailType.SHIFT)
        }

        @Test
        fun `Should return 'shift' for overtime data`() {
            
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(7,15)), day1.atTime(LocalTime.of(12,30)), "Present"),
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(12,30)), day1.atTime(LocalTime.of(13,30)), "Break (Unpaid)"),
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(13,30)), day1.atTime(LocalTime.of(17,0)), "Present")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.shiftType).isEqualTo(DetailType.SHIFT)
        }

        @Test
        fun `Should not return things for a different dates`() {
            
            val day2 = day1.plusDays(1)
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(7,15)), day1.atTime(LocalTime.of(12,30)), "Present"),
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(12,30)), day1.atTime(LocalTime.of(13,30)), "Break (Unpaid)"),
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(13,30)), day1.atTime(LocalTime.of(17,0)), "Present")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day2, day2, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day2), Optional.of(day2))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day2, day2, 1, "xyz") }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day2)
            assertThat(dayModel.shiftType).isEqualTo(DetailType.NONE)
            assertThat(dayModel.details).hasSize(0)

        }

        @Test
        fun `Should identify Shift start`() {
            
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(7,15)), day1.atTime(LocalTime.of(12,30)), "Present"),
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(12,30)), day1.atTime(LocalTime.of(13,30)), "Break (Unpaid)"),
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(13,30)), day1.atTime(LocalTime.of(17,0)), "Present")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            
            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.shiftType).isEqualTo(DetailType.SHIFT)

            val overtimeStartTask = dayModel.details.first{ it.displayType == DetailDisplayType.OVERTIME_DAY_START}
            assertThat(overtimeStartTask.displayTypeTime).isEqualTo(day1.atTime(LocalTime.of(7,15)))
            assertThat(overtimeStartTask.finishDuration).isNull()
        }

        @Test
        fun `Should identify Shift end`() {
            
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(7,15)), day1.atTime(LocalTime.of(12,30)), "Present"),
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(12,30)), day1.atTime(LocalTime.of(13,30)), "Break (Unpaid)"),
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(13,30)), day1.atTime(LocalTime.of(17,0)), "Present")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            
            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.shiftType).isEqualTo(DetailType.SHIFT)

            val overtimeStartTask = dayModel.details.first{ it.displayType == DetailDisplayType.OVERTIME_DAY_FINISH}
            assertThat(overtimeStartTask.displayTypeTime).isEqualTo(day1.atTime(LocalTime.of(17,0)))
            assertThat(overtimeStartTask.finishDuration).isEqualTo("8h 45m")
        }

        @Test
        fun `Should identify Overtime Shift start`() {
            
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(7,15)), day1.atTime(LocalTime.of(12,30)), "Present"),
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(12,30)), day1.atTime(LocalTime.of(13,30)), "Break (Unpaid)"),
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(13,30)), day1.atTime(LocalTime.of(17,0)), "Present")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            
            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.shiftType).isEqualTo(DetailType.SHIFT)

            val overtimeStartTask = dayModel.details.first{ it.displayType == DetailDisplayType.OVERTIME_DAY_START}
            assertThat(overtimeStartTask.displayTypeTime).isEqualTo(day1.atTime(LocalTime.of(7,15)))
            assertThat(overtimeStartTask.finishDuration).isNull()
        }

        @Test
        fun `Should identify Overtime Shift end`() {
            
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(7,15)), day1.atTime(LocalTime.of(12,30)), "Present"),
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(12,30)), day1.atTime(LocalTime.of(13,30)), "Break (Unpaid)"),
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(13,30)), day1.atTime(LocalTime.of(17,0)), "Present")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            
            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.shiftType).isEqualTo(DetailType.SHIFT)

            val overtimeStartTask = dayModel.details.first{ it.displayType == DetailDisplayType.OVERTIME_DAY_FINISH}
            assertThat(overtimeStartTask.displayTypeTime).isEqualTo(day1.atTime(LocalTime.of(17,0)))
            assertThat(overtimeStartTask.finishDuration).isEqualTo("8h 45m")
        }

        @Test
        fun `Should identify Night Shift start`() {
            
            val day2 = day1.plusDays(1)
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(20,15)), day2.atTime(LocalTime.of(12,30)), "Night OSG")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day2, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day2))

            
            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day2, 1, "xyz") }

            assertThat(dayModelList).hasSize(2)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.shiftType).isEqualTo(DetailType.SHIFT)

            val overtimeStartTask = dayModel.details.first{ it.displayType == DetailDisplayType.OVERTIME_NIGHT_START}
            assertThat(overtimeStartTask.displayTypeTime).isEqualTo(day1.atTime(LocalTime.of(20,15)))
            assertThat(overtimeStartTask.finishDuration).isNull()
        }

        @Test
        fun `Should identify Night Shift end`() {
            
            val day2 = day1.plusDays(1)
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(20,15)), day2.atTime(LocalTime.of(12,30)), "Night OSG")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day2, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day2))

            
            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day2, 1, "xyz") }

            assertThat(dayModelList).hasSize(2)

            val dayModel = dayModelList.last()
            assertThat(dayModel.date).isEqualTo(day2)
            assertThat(dayModel.shiftType).isEqualTo(DetailType.SHIFT)

            val overtimeStartTask = dayModel.details.first{ it.displayType == DetailDisplayType.OVERTIME_NIGHT_FINISH}
            assertThat(overtimeStartTask.displayTypeTime).isEqualTo(day2.atTime(LocalTime.of(12,30)))
            assertThat(overtimeStartTask.finishDuration).isEqualTo("16h 15m")
        }

        @Test
        fun `Should identify Overtime Night Shift start`() {
            
            val day2 = day1.plusDays(1)
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(20,15)), day2.atTime(LocalTime.of(12,30)), "Night OSG")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day2, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day2))

            
            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day2, 1, "xyz") }

            assertThat(dayModelList).hasSize(2)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.shiftType).isEqualTo(DetailType.SHIFT)

            val overtimeStartTask = dayModel.details.first{ it.displayType == DetailDisplayType.OVERTIME_NIGHT_START}
            assertThat(overtimeStartTask.displayTypeTime).isEqualTo(day1.atTime(LocalTime.of(20,15)))
            assertThat(overtimeStartTask.finishDuration).isNull()
        }

        @Test
        fun `Should identify Overtime Night Shift end`() {
            
            val day2 = day1.plusDays(1)
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(20,15)), day2.atTime(LocalTime.of(12,30)), "Night OSG")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day2, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day2))

            
            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day2, 1, "xyz") }

            assertThat(dayModelList).hasSize(2)

            val dayModel = dayModelList.last()
            assertThat(dayModel.date).isEqualTo(day2)
            assertThat(dayModel.shiftType).isEqualTo(DetailType.SHIFT)

            val overtimeStartTask = dayModel.details.first{ it.displayType == DetailDisplayType.OVERTIME_NIGHT_FINISH}
            assertThat(overtimeStartTask.displayTypeTime).isEqualTo(day2.atTime(LocalTime.of(12,30)))
            assertThat(overtimeStartTask.finishDuration).isEqualTo("16h 15m")
        }
    }
}