package uk.gov.justice.digital.hmpps.cmd.api.service

import com.microsoft.applicationinsights.TelemetryClient
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
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrClient
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrModifiedDetailDto
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailModificationType
import uk.gov.justice.digital.hmpps.cmd.api.model.DryRunNotification
import uk.gov.justice.digital.hmpps.cmd.api.model.Prison
import uk.gov.justice.digital.hmpps.cmd.api.repository.DryRunNotificationRepository
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import uk.gov.service.notify.NotificationClient
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@ExtendWith(MockKExtension::class)
@DisplayName("DryRunNotification Service tests Generate Shift task")
internal class DryRunNotificationServiceTest_Generate_Shift_Task {
  private val dryRunNotificationRepository: DryRunNotificationRepository = mockk(relaxUnitFun = true)
  private val userPreferenceService: UserPreferenceService = mockk(relaxUnitFun = true)
  private val prisonService: PrisonService = mockk(relaxUnitFun = true)
  private val authenticationFacade: AuthenticationFacade = mockk(relaxUnitFun = true)
  private val notifyClient: NotificationClient = mockk(relaxUnitFun = true)
  private val csrClient: CsrClient = mockk(relaxUnitFun = true)
  private val clock = Clock.fixed(Instant.parse("2022-04-01T10:00:00Z"), ZoneId.systemDefault())
  private val service = DryRunNotificationService(
    dryRunNotificationRepository,
    userPreferenceService,
    clock,
    authenticationFacade,
    3,
    notifyClient,
    prisonService,
    csrClient,
    TelemetryClient()
  )

  @BeforeEach
  fun resetAllMocks() {
    clearMocks(dryRunNotificationRepository)
    clearMocks(userPreferenceService)
    clearMocks(notifyClient)
    clearMocks(csrClient)
  }

  @Nested
  @DisplayName("Generate and save Notification tests")
  inner class GenerateAndSaveNotificationTests {

    @BeforeEach
    fun `set up prison fetching`() {
      val prison1 = Prison("ABC", "Main Gate", "Midgar Central", 1)
      every { prisonService.getAllPrisons() } returns listOf(prison1)
    }

    @Test
    fun `Should disregard Shift Task if it exists in our db`() {
      val today = LocalDate.now(clock)
      val quantumId = "CSTRIFE_GEN"
      val start = today.atStartOfDay().plusSeconds(123L)
      val end = today.atStartOfDay().plusSeconds(456L)
      val task = "Guard Duty"
      val shiftType = ShiftType.SHIFT
      val dto1 = CsrModifiedDetailDto(
        1,
        quantumId,
        today.atStartOfDay(),
        shiftType,
        start,
        end,
        task,
        DetailModificationType.ADD
      )
      every { csrClient.getModified(any()) } returns listOf(dto1)

      every {
        dryRunNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          start,
          any(),
          today.atStartOfDay()
        )
      } returns 1

      service.dryRunNotifications(1)

      verify(exactly = 0) { dryRunNotificationRepository.saveAll<DryRunNotification>(allAny()) }
    }

    @Test
    fun `Should add multiple Shift Task notifications for one user`() {
      val today = LocalDate.now(clock)
      val quantumId = "CSTRIFE_GEN"
      val start = today.atStartOfDay().plusSeconds(123L)
      val end = today.atStartOfDay().plusSeconds(456L)
      val task = "Guard Duty"
      val dto1 = CsrModifiedDetailDto(
        1,
        quantumId,
        today.atStartOfDay(),
        ShiftType.SHIFT,
        start.plusSeconds(10),
        end,
        task,
        DetailModificationType.EDIT
      )

      val dto2 = CsrModifiedDetailDto(
        2,
        quantumId,
        today.atStartOfDay(),
        ShiftType.SHIFT,
        start.plusSeconds(20),
        end,
        task,
        DetailModificationType.EDIT
      )

      val dto3 = CsrModifiedDetailDto(
        3,
        quantumId,
        today.atStartOfDay(),
        ShiftType.SHIFT,
        start.plusSeconds(30),
        end,
        task,
        DetailModificationType.EDIT
      )

      every { csrClient.getModified(any()) } returns listOf(dto1, dto2, dto3)

      every {
        dryRunNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          any(),
          any(),
          today.atStartOfDay()
        )
      } returns 0

      val results = mutableListOf<Collection<DryRunNotification>>()
      every { dryRunNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.dryRunNotifications(1)

      val notification1 = DryRunNotification(
        0,
        quantumId,
        today.atStartOfDay(),
        start.plusSeconds(10),
        end,
        task,
        ShiftType.SHIFT,
        DetailModificationType.EDIT,
        false
      )
      val notification2 = DryRunNotification(
        0,
        quantumId,
        today.atStartOfDay(),
        start.plusSeconds(20),
        end,
        task,
        ShiftType.SHIFT,
        DetailModificationType.EDIT,
        false
      )
      val notification3 = DryRunNotification(
        0,
        quantumId,
        today.atStartOfDay(),
        start.plusSeconds(30),
        end,
        task,
        ShiftType.SHIFT,
        DetailModificationType.EDIT,
        false
      )
      assertThat(results[0]).isEqualTo(listOf(notification1, notification2, notification3))
    }

    @Test
    fun `Should not add multiple duplicate Shift Task notifications for one user`() {
      val today = LocalDate.now(clock)
      val quantumId = "CSTRIFE_GEN"
      val start = today.atStartOfDay().plusSeconds(123L)
      val end = today.atStartOfDay().plusSeconds(456L)
      val task = "Guard Duty"
      val dto1 = CsrModifiedDetailDto(
        1,
        quantumId,
        today.atStartOfDay(),
        ShiftType.SHIFT,
        start,
        end,
        task,
        DetailModificationType.ADD
      )

      val dto2 = CsrModifiedDetailDto(
        2,
        quantumId,
        today.atStartOfDay(),
        ShiftType.SHIFT,
        start,
        end,
        task,
        DetailModificationType.ADD
      )

      val dto3 = CsrModifiedDetailDto(
        3,
        quantumId,
        today.atStartOfDay(),
        ShiftType.SHIFT,
        start,
        end,
        task,
        DetailModificationType.ADD
      )

      every { csrClient.getModified(any()) } returns listOf(dto1, dto2, dto3)

      every {
        dryRunNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          any(),
          any(),
          today.atStartOfDay()
        )
      } returns 0

      val results = mutableListOf<Collection<DryRunNotification>>()
      every { dryRunNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.dryRunNotifications(1)
      val notification1 = DryRunNotification(
        0,
        quantumId,
        today.atStartOfDay(),
        start,
        end,
        task,
        ShiftType.SHIFT,
        DetailModificationType.ADD,
        false
      )
      assertThat(results[0]).isEqualTo(listOf(notification1))
    }

    @Test
    fun `Should filter out duplicate Shift Task notifications when there is an OverTime one too`() {
      val today = LocalDate.now(clock)
      val quantumId = "CSTRIFE_GEN"
      val start = today.atStartOfDay().plusSeconds(123L)
      val end = today.atStartOfDay().plusSeconds(456L)
      val task = "Guard Duty"
      val dto1 = CsrModifiedDetailDto(
        1, quantumId,
        today.atStartOfDay(),
        ShiftType.OVERTIME,
        start,
        end,
        task,
        DetailModificationType.EDIT
      )

      val dto2 = CsrModifiedDetailDto(
        2, quantumId,
        today.atStartOfDay(),
        ShiftType.SHIFT,
        start,
        end,
        task,
        DetailModificationType.EDIT
      )

      val dto3 = CsrModifiedDetailDto(
        3, quantumId,
        today.atStartOfDay(),
        ShiftType.SHIFT,
        start,
        end,
        task,
        DetailModificationType.EDIT
      )

      every { csrClient.getModified(any()) } returns listOf(dto2, dto3, dto1)

      every {
        dryRunNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          any(),
          any(),
          today.atStartOfDay()
        )
      } returns 0

      val results = mutableListOf<Collection<DryRunNotification>>()
      every { dryRunNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.dryRunNotifications(1)
      val notification1 = DryRunNotification(
        0,
        quantumId,
        today.atStartOfDay(),
        start,
        end,
        task,
        ShiftType.SHIFT,
        DetailModificationType.EDIT,
        false
      )
      val notification2 = DryRunNotification(
        0,
        quantumId,
        today.atStartOfDay(),
        start,
        end,
        task,
        ShiftType.OVERTIME,
        DetailModificationType.EDIT,
        false
      )
      assertThat(results[0]).isEqualTo(listOf(notification1, notification2))
    }

    @Test
    fun `Should not add multiple notifications for same shift task with different modified times for one user`() {
      val today = LocalDate.now(clock)
      val quantumId = "CSTRIFE_GEN"
      val start = today.atStartOfDay().plusSeconds(123L)
      val end = today.atStartOfDay().plusSeconds(456L)
      val task = "Guard Duty"
      val dto1 = CsrModifiedDetailDto(
        1,
        quantumId,
        today.atStartOfDay(),
        ShiftType.SHIFT,
        start,
        end,
        task,
        DetailModificationType.ADD
      )
      val dto2 = dto1.copy(id = 2, shiftModified = today.atStartOfDay().plusSeconds(5))
      val dto3 = dto1.copy(id = 3, shiftModified = today.atStartOfDay().plusSeconds(10))

      every { csrClient.getModified(any()) } returns listOf(dto1, dto2, dto3)

      every {
        dryRunNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          any(),
          any(),
          today.atStartOfDay()
        )
      } returns 0

      val results = mutableListOf<Collection<DryRunNotification>>()
      every { dryRunNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.dryRunNotifications(1)
      val notification1 = DryRunNotification(
        0,
        quantumId,
        today.atStartOfDay(),
        start,
        end,
        task,
        ShiftType.SHIFT,
        DetailModificationType.ADD,
        false
      )

      assertThat(results[0]).isEqualTo(listOf(notification1))
    }

    @Test
    fun `Should save edit Shift Task notification types`() {
      val today = LocalDate.now(clock)
      val quantumId = "CSTRIFE_GEN"
      val start = today.atStartOfDay().plusSeconds(123L)
      val end = today.atStartOfDay().plusSeconds(456L)
      val task = "Guard Duty"
      val dto1 = CsrModifiedDetailDto(
        1, quantumId,
        today.atStartOfDay(),
        ShiftType.SHIFT,
        start,
        end,
        task,
        DetailModificationType.EDIT
      )

      every { csrClient.getModified(any()) } returns listOf(dto1)

      every {
        dryRunNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          start,
          any(),
          today.atStartOfDay()
        )
      } returns 0

      val results = mutableListOf<Collection<DryRunNotification>>()
      every { dryRunNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.dryRunNotifications(1)
      val expected = DryRunNotification(
        0,
        quantumId,
        today.atStartOfDay(),
        start,
        end,
        task,
        ShiftType.SHIFT,
        DetailModificationType.EDIT,
        false
      )
      assertThat(results[0]).isEqualTo(listOf(expected))
    }

    @Test
    fun `Should save Add shift Task notification types if not exist in the DB`() {
      val today = LocalDate.now(clock)
      val quantumId = "CSTRIFE_GEN"
      val start = today.atStartOfDay().plusSeconds(123L)
      val end = today.atStartOfDay().plusSeconds(456L)
      val task = "Guard Duty"
      val dto1 = CsrModifiedDetailDto(
        1, quantumId,
        today.atStartOfDay(),
        ShiftType.SHIFT,
        start,
        end,
        task,
        DetailModificationType.ADD
      )

      every { csrClient.getModified(any()) } returns listOf(dto1)

      every {
        dryRunNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          start,
          any(),
          today.atStartOfDay()
        )
      } returns 0

      val results = mutableListOf<Collection<DryRunNotification>>()
      every { dryRunNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.dryRunNotifications(1)

      val expected = DryRunNotification(
        0,
        quantumId,
        today.atStartOfDay(),
        start,
        end,
        task,
        ShiftType.SHIFT,
        DetailModificationType.ADD,
        false
      )
      assertThat(results[0]).isEqualTo(listOf(expected))
    }

    @Test
    fun `Should save remove Shift Task notification types if not exist in the DB`() {
      val today = LocalDate.now(clock)
      val quantumId = "CSTRIFE_GEN"
      val start = today.atStartOfDay().plusSeconds(123L)
      val end = today.atStartOfDay().plusSeconds(456L)
      val task = "Guard Duty"
      val dto1 = CsrModifiedDetailDto(
        1, quantumId,
        today.atStartOfDay(),
        ShiftType.SHIFT,
        start,
        end,
        task,
        DetailModificationType.DELETE
      )

      every { csrClient.getModified(any()) } returns listOf(dto1)

      every {
        dryRunNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          start,
          any(),
          today.atStartOfDay()
        )
      } returns 0

      val results = mutableListOf<Collection<DryRunNotification>>()
      every { dryRunNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.dryRunNotifications(1)

      val expected = DryRunNotification(
        0,
        quantumId,
        today.atStartOfDay(),
        start,
        end,
        task,
        ShiftType.SHIFT,
        DetailModificationType.DELETE,
        false
      )
      assertThat(results[0]).isEqualTo(listOf(expected))
    }

    @Test
    fun `Should disregard unprocessed Shift Task Notification duplicates`() {
      val today = LocalDate.now(clock)
      val quantumId = "CSTRIFE_GEN"
      val start = today.atStartOfDay().plusSeconds(123L)
      val end = today.atStartOfDay().plusSeconds(456L)
      val task = "Guard Duty"
      val dto1 = CsrModifiedDetailDto(
        1, quantumId,
        today.atStartOfDay(),
        ShiftType.SHIFT,
        start,
        end,
        task,
        DetailModificationType.ADD
      )
      every { csrClient.getModified(any()) } returns listOf(dto1)

      every {
        dryRunNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          any(),
          ShiftType.SHIFT,
          today.atStartOfDay()
        )
      } returns 1

      service.dryRunNotifications(1)

      verify(exactly = 0) { dryRunNotificationRepository.saveAll<DryRunNotification>(allAny()) }
    }

    @Test
    fun `Should do nothing if there are no notifications`() {
      every { csrClient.getModified(any()) } returns listOf()

      service.dryRunNotifications(1)

      verify(exactly = 0) { dryRunNotificationRepository.saveAll<DryRunNotification>(allAny()) }
    }
  }
}
