package uk.gov.justice.digital.hmpps.cmd.api.service

import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrClient
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrDetailDto
import uk.gov.justice.digital.hmpps.cmd.api.domain.FullDayActivityType
import uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.cmd.api.model.Prison
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Optional

@ExtendWith(MockKExtension::class)
@DisplayName("Shift Service Full Day Type tests")
internal class ShiftServiceTest_DayModelFullDayType {
  private val csrApiClient: CsrClient = mockk(relaxUnitFun = true)
  private val prisonService: PrisonService = mockk(relaxUnitFun = true)
  private val authenticationFacade: AuthenticationFacade = mockk(relaxUnitFun = true)
  private val clock = Clock.fixed(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
  private val service = ShiftService(prisonService, csrApiClient, clock, authenticationFacade)

  private val day1 = LocalDate.now(clock)
  private val day2 = day1.plusDays(1)

  @BeforeEach
  fun resetAllMocks() {
    clearMocks(csrApiClient)
    clearMocks(prisonService)

    every { prisonService.getPrisonForUser() } returns Prison("prison", "", "", 1)
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
    fun `Should return Rest_Day as Full Day Type for Rest_Day`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(0, 0)), day1.atTime(LocalTime.of(23, 59, 59)), "Rest Day")
      )

      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.REST_DAY)
    }

    @Test
    fun `Should return Rest_Day as Full Day Type for Rest_Day with a break`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(0, 0)), day1.atTime(LocalTime.of(12, 30)), "Rest Day"),
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(12, 30)), day1.atTime(LocalTime.of(13, 0)), "Break")
      )

      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.REST_DAY)
    }

    @Test
    fun `Should return Rest_Day as Full Day Type for Rest_Day with a Night shift end`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.SHIFT, day2.atTime(LocalTime.of(0, 0)), day2.atTime(LocalTime.of(12, 30)), "Rest Day"),
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(12, 30)), day2.atTime(LocalTime.of(13, 0)), "Nights")
      )

      every { csrApiClient.getDetailsForUser(day2, day2, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day2), Optional.of(day2))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day2, day2, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day2)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.REST_DAY)
    }

    @Test
    fun `Should return Holiday as Full Day Type for Holiday`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(0, 0)), day1.atTime(LocalTime.of(23, 59, 59)), "Annual Leave")
      )

      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.HOLIDAY)
    }

    @Test
    fun `Should return Holiday as Full Day Type for Holiday with a break`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(0, 0)), day1.atTime(LocalTime.of(12, 30)), "Annual Leave"),
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(12, 30)), day1.atTime(LocalTime.of(13, 0)), "Break")
      )

      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.HOLIDAY)
    }

    @Test
    fun `Should return Holiday as Full Day Type for Holiday with a Night shift end`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.SHIFT, day2.atTime(LocalTime.of(0, 0)), day2.atTime(LocalTime.of(12, 30)), "Annual Leave"),
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(12, 30)), day2.atTime(LocalTime.of(13, 0)), "Nights")
      )

      every { csrApiClient.getDetailsForUser(day2, day2, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day2), Optional.of(day2))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day2, day2, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day2)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.HOLIDAY)
    }

    @Test
    fun `Should return Shift as Full Day Type for Holiday with other Unspecific tasks`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(7, 0)), day1.atTime(LocalTime.of(10, 0)), "Annual Leave"),
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(19, 0)), day1.atTime(LocalTime.of(20, 0)), "Door Guard")
      )

      every { prisonService.getPrisonForUser() } returns Prison("prison", "", "", 1)
      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
    }

    @Test
    fun `Should return Illness as Full Day Type for Illness`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(0, 0)), day1.atTime(LocalTime.of(23, 59, 59)), "Sick")
      )

      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.ILLNESS)
    }

    @Test
    fun `Should return Absence as Full Day Type for Absence`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(0, 0)), day1.atTime(LocalTime.of(23, 59, 59)), "Absence")
      )

      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.ABSENCE)
    }

    @Test
    fun `Should return Absence as Full Day Type for Absence with a break`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(0, 0)), day1.atTime(LocalTime.of(12, 30)), "Absence"),
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(12, 30)), day1.atTime(LocalTime.of(13, 0)), "Break")
      )

      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.ABSENCE)
    }

    @Test
    fun `Should return Absence as Full Day Type for Absence with a Night shift end`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.SHIFT, day2.atTime(LocalTime.of(0, 0)), day2.atTime(LocalTime.of(12, 30)), "Absence"),
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(12, 30)), day2.atTime(LocalTime.of(13, 0)), "Nights")
      )

      every { csrApiClient.getDetailsForUser(day2, day2, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day2), Optional.of(day2))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day2, day2, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day2)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.ABSENCE)
    }

    @Test
    fun `Should return TOIL as Full Day Type for TOIL`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(7, 0)), day1.atTime(LocalTime.of(11, 0)), "Toil")
      )

      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.TOIL)
    }

    @Test
    fun `Should return TOIL as Full Day Type for TOIL if it is a Night Start`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(7, 0)), day2.atTime(LocalTime.of(11, 0)), "Toil")
      )

      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.TOIL)
    }

    @Test
    fun `Should return SHIFT as Full Day Type for TOIL if not the first task`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(6, 0)), day1.atTime(LocalTime.of(7, 0)), "Other"),
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(7, 0)), day1.atTime(LocalTime.of(11, 0)), "Toil")
      )

      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
    }

    @Test
    fun `Should return SHIFT as Full Day Activity for TOIL`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(0, 0), day1.atTime(LocalTime.of(0, 0)), "TOIL"),
        CsrDetailDto(ShiftType.SHIFT, day2.atTime(LocalTime.of(7, 0)), day2.atTime(LocalTime.of(11, 0)), "Other")
      )

      every { csrApiClient.getDetailsForUser(day1, day2, 1, "xyz") } returns shifts
      val fullDayActivityModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day2))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day2, 1, "xyz") }

      assertThat(fullDayActivityModelList).hasSize(2)
      val fullDayActivityModel = fullDayActivityModelList.first()
      assertThat(fullDayActivityModel.date).isEqualTo(day1)
      assertThat(fullDayActivityModel.shiftType).isEqualTo(FullDayActivityType.TOIL)
    }

    @Test
    fun `Should return SECONDMENT as Full Day Type for SECONDMENT`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(7, 0)), day1.atTime(LocalTime.of(11, 0)), "detached Duty")
      )

      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SECONDMENT)
    }

    @Test
    fun `Should return Secondment as Full Day Type for Secondment if it is a Night Start`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(7, 0)), day2.atTime(LocalTime.of(11, 0)), "Detached Duty")
      )

      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SECONDMENT)
    }

    @Test
    fun `Should return SHIFT as Full Day Type for Secondment if not the first task`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(6, 0)), day1.atTime(LocalTime.of(7, 0)), "Other"),
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(7, 0)), day1.atTime(LocalTime.of(11, 0)), "Secodment")
      )

      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
    }

    @Test
    fun `Should return type as Full Day Type for Training Internal`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(7, 15)), day1.atTime(LocalTime.of(12, 30)), "Training - Internal")
      )

      every { prisonService.getPrisonForUser() } returns Prison("prison", "", "", 1)
      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.TRAINING_INTERNAL)
    }

    @Test
    fun `Should return Training Internal as Full Day Type for Training Internal`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(7, 0)), day1.atTime(LocalTime.of(11, 0)), "Training - Internal")
      )

      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.TRAINING_INTERNAL)
    }

    @Test
    fun `Should return Training Internal as Full Day Type for Training Internal if it is a Night Start`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(7, 0)), day2.atTime(LocalTime.of(11, 0)), "Training - Internal")
      )

      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.TRAINING_INTERNAL)
    }

    @Test
    fun `Should return SHIFT as Full Day Type for Training Internal if not the first task`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(6, 0)), day1.atTime(LocalTime.of(7, 0)), "Other"),
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(7, 0)), day1.atTime(LocalTime.of(11, 0)), "Training - Internal")
      )

      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
    }

    @Test
    fun `Should return Training Internal as Full Day Type for Training External`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(7, 0)), day1.atTime(LocalTime.of(11, 0)), "Training - External")
      )

      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.TRAINING_EXTERNAL)
    }

    @Test
    fun `Should return Training Internal as Full Day Type for Training External if it is a Night Start`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(7, 0)), day2.atTime(LocalTime.of(11, 0)), "Training - External")
      )

      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.TRAINING_EXTERNAL)
    }

    @Test
    fun `Should return SHIFT as Full Day Type for Training External if not the first task`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(6, 0)), day1.atTime(LocalTime.of(7, 0)), "Other"),
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(7, 0)), day1.atTime(LocalTime.of(11, 0)), "Training - External")
      )

      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
    }

    @Test
    fun `Should return Shift as Full Day Type if no rules met`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.SHIFT, day1.atTime(LocalTime.of(7, 0)), day1.atTime(LocalTime.of(10, 0)), "On Call")
      )

      every { prisonService.getPrisonForUser() } returns Prison("prison", "", "", 1)
      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
    }

    @Test
    fun `Should return NONE as Full Day Type if no data`() {

      every { prisonService.getPrisonForUser() } returns Prison("prison", "", "", 1)
      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns listOf()

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.NONE)
    }

    @Test
    fun `Overtime - Should return SHIFT as Full Day Type for full day SHIFT`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.OVERTIME, day1.atTime(LocalTime.of(0, 0)), day1.atTime(LocalTime.of(23, 59, 59)), "My Activity")
      )

      every { prisonService.getPrisonForUser() } returns Prison("prison", "", "", 1)
      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
    }

    @Test
    fun `Overtime - Should return SHIFT as Full Day Type for Training Internal`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.OVERTIME, day1.atTime(LocalTime.of(7, 15)), day1.atTime(LocalTime.of(12, 30)), "Training - Internal")
      )

      every { prisonService.getPrisonForUser() } returns Prison("prison", "", "", 1)
      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
    }

    @Test
    fun `Overtime Should return SHIFT as Full Day Type for Training external`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.OVERTIME, day1.atTime(LocalTime.of(7, 15)), day1.atTime(LocalTime.of(12, 30)), "Training - External")
      )

      every { prisonService.getPrisonForUser() } returns Prison("prison", "", "", 1)
      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
    }

    @Test
    fun `Overtime - Should return SHIFT as Full Day Type for Absence`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.OVERTIME, day1.atTime(LocalTime.of(0, 0)), day1.atTime(LocalTime.of(23, 59, 59)), "Absence")
      )

      every { prisonService.getPrisonForUser() } returns Prison("prison", "", "", 1)
      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
    }

    @Test
    fun `Overtime - Should return SHIFT as Full Day Type for full day Holiday`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.OVERTIME, day1.atTime(LocalTime.of(0, 0)), day1.atTime(LocalTime.of(23, 59, 59)), "Annual Leave")
      )

      every { prisonService.getPrisonForUser() } returns Prison("prison", "", "", 1)
      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
    }

    @Test
    fun `Overtime - Should return SHIFT as Full Day Type for Holiday with not other Unspecific tasks`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.OVERTIME, day1.atTime(LocalTime.of(7, 0)), day1.atTime(LocalTime.of(10, 0)), "Annual Leave")
      )

      every { prisonService.getPrisonForUser() } returns Prison("prison", "", "", 1)
      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
    }

    @Test
    fun `Overtime - Should return SHIFT as Full Day Type for Holiday with other Unspecific tasks`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.OVERTIME, day1.atTime(LocalTime.of(7, 0)), day1.atTime(LocalTime.of(10, 0)), "Annual Leave"),
        CsrDetailDto(ShiftType.OVERTIME, day1.atTime(LocalTime.of(19, 0)), day1.atTime(LocalTime.of(20, 0)), "Door Guard")
      )

      every { prisonService.getPrisonForUser() } returns Prison("prison", "", "", 1)
      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
    }

    @Test
    fun `Overtime - Should return SHIFT as Full Day Type for Illness`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.OVERTIME, day1.atTime(LocalTime.of(7, 0)), day1.atTime(LocalTime.of(10, 0)), "Sick")
      )

      every { prisonService.getPrisonForUser() } returns Prison("prison", "", "", 1)
      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
    }

    @Test
    fun `Overtime - Should return SHIFT as Full Day Type if no rules met`() {

      val shifts = listOf(
        CsrDetailDto(ShiftType.OVERTIME, day1.atTime(LocalTime.of(7, 0)), day1.atTime(LocalTime.of(10, 0)), "On Call")
      )

      every { prisonService.getPrisonForUser() } returns Prison("prison", "", "", 1)
      every { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") } returns shifts

      val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

      verify { prisonService.getPrisonForUser() }
      verify { csrApiClient.getDetailsForUser(day1, day1, 1, "xyz") }

      assertThat(dayModelList).hasSize(1)

      val dayModel = dayModelList.first()
      assertThat(dayModel.date).isEqualTo(day1)
      assertThat(dayModel.shiftType).isEqualTo(FullDayActivityType.SHIFT)
    }
  }
}
