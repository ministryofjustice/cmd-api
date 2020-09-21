package uk.gov.justice.digital.hmpps.cmd.api.service

import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrClient
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrDetailDto
import uk.gov.justice.digital.hmpps.cmd.api.model.Prison
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.DetailParentType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.FullDayActivityType
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

@ExtendWith(MockKExtension::class)
@DisplayName("Shift Service Full Day Type tests")
internal class ShiftServiceTest_DayModelFullDayType {
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
        fun `Should return activity as Full Day Type for full day Unspecific`() {
            
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.SHIFT, day1.atTime(LocalTime.of(0,0)), day1.atTime(LocalTime.of(23,59,59)), "My Activity")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))
            
            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
        }

        @Test
        fun `Should return type as Full Day Type for Training Internal`() {
            
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.SHIFT, day1.atTime(LocalTime.of(7,15)), day1.atTime(LocalTime.of(12,30)), "Training - Internal")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.TRAINING_INTERNAL)
        }

        @Test
        fun `Should return type as Full Day Type for Training external`() {
            
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.SHIFT, day1.atTime(LocalTime.of(7,15)), day1.atTime(LocalTime.of(12,30)), "Training - External")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.TRAINING_EXTERNAL)
        }

        @Test
        fun `Should return activity as Full Day Type for Absence`() {
            
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.SHIFT, day1.atTime(LocalTime.of(0,0)), day1.atTime(LocalTime.of(23,59,59)), "Absence")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.ABSENCE)
        }

        @Test
        fun `Should return Holiday as Full Day Type for full day Holiday`() {
            
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.SHIFT, day1.atTime(LocalTime.of(0,0)), day1.atTime(LocalTime.of(23,59,59)), "Annual Leave")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.HOLIDAY)
        }

        @Test
        fun `Should return Holiday as Full Day Type for Holiday with not other Unspecific tasks`() {
            
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.SHIFT, day1.atTime(LocalTime.of(7,0)), day1.atTime(LocalTime.of(10,0)), "Annual Leave")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.HOLIDAY)
        }

        @Test
        fun `Should return Shift as Full Day Type for Holiday with other Unspecific tasks`() {
            
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.SHIFT, day1.atTime(LocalTime.of(7,0)), day1.atTime(LocalTime.of(10,0)), "Annual Leave"),
                    CsrDetailDto(DetailParentType.SHIFT, day1.atTime(LocalTime.of(19,0)), day1.atTime(LocalTime.of(20,0)), "Door Guard")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
        }

        @Test
        fun `Should return Illness as Full Day Type for Illness`() {
            
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.SHIFT, day1.atTime(LocalTime.of(7,0)), day1.atTime(LocalTime.of(10,0)), "Sick")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.ILLNESS)
        }

        @Test
        fun `Should return Shift as Full Day Type if no rules met`() {
            
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.SHIFT, day1.atTime(LocalTime.of(7,0)), day1.atTime(LocalTime.of(10,0)), "On Call")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
        }

        @Test
        fun `Overtime - Should return SHIFT as Full Day Type for full day SHIFT`() {
            
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(0,0)), day1.atTime(LocalTime.of(23,59,59)), "My Activity")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
        }

        @Test
        fun `Overtime - Should return SHIFT as Full Day Type for Training Internal`() {
            
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(7,15)), day1.atTime(LocalTime.of(12,30)), "Training - Internal")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
        }

        @Test
        fun `Overtime Should return SHIFT as Full Day Type for Training external`() {
            
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(7,15)), day1.atTime(LocalTime.of(12,30)), "Training - External")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
        }

        @Test
        fun `Overtime - Should return SHIFT as Full Day Type for Absence`() {
            
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(0,0)), day1.atTime(LocalTime.of(23,59,59)), "Absence")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
        }

        @Test
        fun `Overtime - Should return SHIFT as Full Day Type for full day Holiday`() {
            
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(0,0)), day1.atTime(LocalTime.of(23,59,59)), "Annual Leave")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
        }

        @Test
        fun `Overtime - Should return SHIFT as Full Day Type for Holiday with not other Unspecific tasks`() {
            
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(7,0)), day1.atTime(LocalTime.of(10,0)), "Annual Leave")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
        }

        @Test
        fun `Overtime - Should return SHIFT as Full Day Type for Holiday with other Unspecific tasks`() {
            
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(7,0)), day1.atTime(LocalTime.of(10,0)), "Annual Leave"),
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(19,0)), day1.atTime(LocalTime.of(20,0)), "Door Guard")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
        }

        @Test
        fun `Overtime - Should return SHIFT as Full Day Type for Illness`() {
            
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(7,0)), day1.atTime(LocalTime.of(10,0)), "Sick")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
        }

        @Test
        fun `Overtime - Should return SHIFT as Full Day Type if no rules met`() {
            
            val shifts = listOf(
                    CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(7,0)), day1.atTime(LocalTime.of(10,0)), "On Call")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
        }

    }
} 