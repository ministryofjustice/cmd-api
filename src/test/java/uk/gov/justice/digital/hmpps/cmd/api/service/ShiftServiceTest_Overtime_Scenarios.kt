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
@DisplayName("Shift Service tests with Overtime")
internal class ShiftServiceTest_Overtime_Scenarios {
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
        fun `Should return a basic day shift with overtime after`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    CsrDetailDto(day1, EntityType.SHIFT, LocalTime.of(7,15).toSecondOfDay().toLong(), LocalTime.of(12,30).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Present"),
                    CsrDetailDto(day1, EntityType.SHIFT, LocalTime.of(12,30).toSecondOfDay().toLong(), LocalTime.of(13,30).toSecondOfDay().toLong(), DetailType.BREAK, "Break (Unpaid)"),
                    CsrDetailDto(day1, EntityType.SHIFT, LocalTime.of(13,30).toSecondOfDay().toLong(), LocalTime.of(17,0).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Present")
            )

            val overtime = listOf(
                    CsrDetailDto(day1, EntityType.OVERTIME, LocalTime.of(17,15).toSecondOfDay().toLong(), LocalTime.of(18,30).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Present"),
                    CsrDetailDto(day1, EntityType.OVERTIME, LocalTime.of(18,30).toSecondOfDay().toLong(), LocalTime.of(19,30).toSecondOfDay().toLong(), DetailType.BREAK, "Break (Unpaid)"),
                    CsrDetailDto(day1, EntityType.OVERTIME, LocalTime.of(19,30).toSecondOfDay().toLong(), LocalTime.of(22,0).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Present")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1) } returns dayShift + overtime

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("Shift")
            assertThat(dayModel.details).hasSize(6)
            // 4 significant events but 6 details total
            assertThat(dayModel.details.filter { it.displayType != null}).hasSize(4)


            val startTask = dayModel.details.first{ it.displayType == TaskDisplayType.DAY_START}
            assertThat(startTask.eventTime).isEqualTo(LocalTime.of(7,15))
            assertThat(startTask.finishDuration).isNull()

            val endTask = dayModel.details.first{ it.displayType == TaskDisplayType.DAY_FINISH}
            assertThat(endTask.eventTime).isEqualTo(LocalTime.of(17,0))
            assertThat(endTask.finishDuration).isEqualTo("8h 45m")

            val overtimeStartTask = dayModel.details.first{ it.displayType == TaskDisplayType.OVERTIME_DAY_START}
            assertThat(overtimeStartTask.eventTime).isEqualTo(LocalTime.of(17,15))
            assertThat(overtimeStartTask.finishDuration).isNull()

            val overtimeEndTask = dayModel.details.first{ it.displayType == TaskDisplayType.OVERTIME_DAY_FINISH}
            assertThat(overtimeEndTask.eventTime).isEqualTo(LocalTime.of(22,0))
            assertThat(overtimeEndTask.finishDuration).isEqualTo("3h 45m")
        }

        @Test
        fun `Should return a basic day shift with overtime before`() {
            val day1 = LocalDate.now(clock)
            val dayShift = listOf(
                    CsrDetailDto(day1, EntityType.SHIFT, LocalTime.of(17,15).toSecondOfDay().toLong(), LocalTime.of(18,30).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Present"),
                    CsrDetailDto(day1, EntityType.SHIFT, LocalTime.of(18,30).toSecondOfDay().toLong(), LocalTime.of(19,30).toSecondOfDay().toLong(), DetailType.BREAK, "Break (Unpaid)"),
                    CsrDetailDto(day1, EntityType.SHIFT, LocalTime.of(19,30).toSecondOfDay().toLong(), LocalTime.of(22,0).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Present")
            )

            val overtime = listOf(
                    CsrDetailDto(day1, EntityType.OVERTIME, LocalTime.of(7,15).toSecondOfDay().toLong(), LocalTime.of(12,30).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Present"),
                    CsrDetailDto(day1, EntityType.OVERTIME, LocalTime.of(12,30).toSecondOfDay().toLong(), LocalTime.of(13,30).toSecondOfDay().toLong(), DetailType.BREAK, "Break (Unpaid)"),
                    CsrDetailDto(day1, EntityType.OVERTIME, LocalTime.of(13,30).toSecondOfDay().toLong(), LocalTime.of(17,0).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Present")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day1, 1) } returns dayShift + overtime

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day1))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day1, 1) }

            assertThat(dayModelList).hasSize(1)

            val dayModel = dayModelList.first()
            assertThat(dayModel.date).isEqualTo(day1)
            assertThat(dayModel.fullDayType).isEqualTo("Shift")
            assertThat(dayModel.details).hasSize(6)
            // 4 significant events but 6 details total
            assertThat(dayModel.details.filter { it.displayType != null}).hasSize(4)

            val overtimeStartTask = dayModel.details.first{ it.displayType == TaskDisplayType.DAY_START}
            assertThat(overtimeStartTask.eventTime).isEqualTo(LocalTime.of(17,15))
            assertThat(overtimeStartTask.finishDuration).isNull()

            val overtimeEndTask = dayModel.details.first{ it.displayType == TaskDisplayType.DAY_FINISH}
            assertThat(overtimeEndTask.eventTime).isEqualTo(LocalTime.of(22,0))
            assertThat(overtimeEndTask.finishDuration).isEqualTo("3h 45m")

            val startTask = dayModel.details.first{ it.displayType == TaskDisplayType.OVERTIME_DAY_START}
            assertThat(startTask.eventTime).isEqualTo(LocalTime.of(7,15))
            assertThat(startTask.finishDuration).isNull()

            val endTask = dayModel.details.first{ it.displayType == TaskDisplayType.OVERTIME_DAY_FINISH}
            assertThat(endTask.eventTime).isEqualTo(LocalTime.of(17,0))
            assertThat(endTask.finishDuration).isEqualTo("8h 45m")
        }

        @Test
        fun `Should return a basic night shift with overtime after`() {
            val day1 = LocalDate.now(clock)
            val day2 = day1.plusDays(1)

            val nightShift = listOf(
                    CsrDetailDto(day1, EntityType.SHIFT, LocalTime.of(20,45).toSecondOfDay().toLong(), LocalTime.of(7,45).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Night OSG")
            )

            val overtime = listOf(
                    CsrDetailDto(day2, EntityType.OVERTIME, LocalTime.of(17,15).toSecondOfDay().toLong(), LocalTime.of(18,30).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Present"),
                    CsrDetailDto(day2, EntityType.OVERTIME, LocalTime.of(18,30).toSecondOfDay().toLong(), LocalTime.of(19,30).toSecondOfDay().toLong(), DetailType.BREAK, "Break (Unpaid)"),
                    CsrDetailDto(day2, EntityType.OVERTIME, LocalTime.of(19,30).toSecondOfDay().toLong(), LocalTime.of(22,0).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Present")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day2, 1) } returns nightShift + overtime

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day2))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day2, 1) }

            assertThat(dayModelList).hasSize(2)

            val dayModel1 = dayModelList.first{ it.date == day1 }
            assertThat(dayModel1.date).isEqualTo(day1)
            assertThat(dayModel1.fullDayType).isEqualTo("Shift")
            assertThat(dayModel1.details).hasSize(1)

            val startTask1 = dayModel1.details.first{ it.displayType == TaskDisplayType.NIGHT_START}
            assertThat(startTask1.eventTime).isEqualTo(LocalTime.of(20,45))
            assertThat(startTask1.finishDuration).isNull()

            val dayModel2 = dayModelList.first{ it.date == day2 }
            assertThat(dayModel2.date).isEqualTo(day2)
            assertThat(dayModel2.fullDayType).isEqualTo("Shift")
            assertThat(dayModel2.details).hasSize(4)
            // 3 significant events but 4 details total
            assertThat(dayModel2.details.filter { it.displayType != null}).hasSize(3)

            val startTask2 = dayModel2.details.first{ it.displayType == TaskDisplayType.NIGHT_FINISH}
            assertThat(startTask2.eventTime).isEqualTo(LocalTime.of(7,45))
            assertThat(startTask2.finishDuration).isEqualTo("11h 00m")

            val overtimeStartTask = dayModel2.details.first{ it.displayType == TaskDisplayType.OVERTIME_DAY_START}
            assertThat(overtimeStartTask.eventTime).isEqualTo(LocalTime.of(17,15))
            assertThat(overtimeStartTask.finishDuration).isNull()

            val overtimeEndTask = dayModel2.details.first{ it.displayType == TaskDisplayType.OVERTIME_DAY_FINISH}
            assertThat(overtimeEndTask.eventTime).isEqualTo(LocalTime.of(22,0))
            assertThat(overtimeEndTask.finishDuration).isEqualTo("3h 45m")
        }

        @Test
        fun `Should return a basic night shift with overtime before`() {
            val day1 = LocalDate.now(clock)
            val day2 = day1.plusDays(1)

            val nightShift = listOf(
                    CsrDetailDto(day1, EntityType.SHIFT, LocalTime.of(20,45).toSecondOfDay().toLong(), LocalTime.of(7,45).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Night OSG")
            )

            val overtime = listOf(
                    CsrDetailDto(day1, EntityType.OVERTIME, LocalTime.of(17,15).toSecondOfDay().toLong(), LocalTime.of(18,30).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Present"),
                    CsrDetailDto(day1, EntityType.OVERTIME, LocalTime.of(18,30).toSecondOfDay().toLong(), LocalTime.of(19,30).toSecondOfDay().toLong(), DetailType.BREAK, "Break (Unpaid)"),
                    CsrDetailDto(day1, EntityType.OVERTIME, LocalTime.of(19,30).toSecondOfDay().toLong(), LocalTime.of(20,0).toSecondOfDay().toLong(), DetailType.UNSPECIFIC, "Present")
            )

            every { prisonService.getPrisonForUser()} returns Prison("prison", "", "", 1)
            every { csrApiClient.getDetailsForUser(day1, day2, 1) } returns nightShift + overtime

            val dayModelList = service.getDetailsForUser(Optional.of(day1), Optional.of(day2))

            verify { prisonService.getPrisonForUser()}
            verify { csrApiClient.getDetailsForUser(day1, day2, 1) }

            assertThat(dayModelList).hasSize(2)

            val dayModel1 = dayModelList.first{ it.date == day1 }
            assertThat(dayModel1.date).isEqualTo(day1)
            assertThat(dayModel1.fullDayType).isEqualTo("Shift")
            assertThat(dayModel1.details).hasSize(4)
            // 3 significant events but 4 details total
            assertThat(dayModel1.details.filter { it.displayType != null}).hasSize(3)

            val overtimeStartTask = dayModel1.details.first{ it.displayType == TaskDisplayType.OVERTIME_DAY_START}
            assertThat(overtimeStartTask.eventTime).isEqualTo(LocalTime.of(17,15))
            assertThat(overtimeStartTask.finishDuration).isNull()

            val overtimeEndTask = dayModel1.details.first{ it.displayType == TaskDisplayType.OVERTIME_DAY_FINISH}
            assertThat(overtimeEndTask.eventTime).isEqualTo(LocalTime.of(20,0))
            assertThat(overtimeEndTask.finishDuration).isEqualTo("1h 45m")

            val startTask1 = dayModel1.details.first{ it.displayType == TaskDisplayType.NIGHT_START}
            assertThat(startTask1.eventTime).isEqualTo(LocalTime.of(20,45))
            assertThat(startTask1.finishDuration).isNull()

            val dayModel2 = dayModelList.first{ it.date == day2 }
            assertThat(dayModel2.date).isEqualTo(day2)
            assertThat(dayModel2.fullDayType).isEqualTo("Shift")
            assertThat(dayModel2.details).hasSize(1)

            val startTask2 = dayModel2.details.first{ it.displayType == TaskDisplayType.NIGHT_FINISH}
            assertThat(startTask2.eventTime).isEqualTo(LocalTime.of(7,45))
            assertThat(startTask2.finishDuration).isEqualTo("11h 00m")
        }
    }
} 