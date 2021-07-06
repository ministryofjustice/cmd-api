package uk.gov.justice.digital.hmpps.cmd.api.service

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrClient
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrModifiedDetailDto
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailModificationType
import uk.gov.justice.digital.hmpps.cmd.api.model.Notification
import uk.gov.justice.digital.hmpps.cmd.api.model.Prison
import uk.gov.justice.digital.hmpps.cmd.api.repository.NotificationRepository
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import uk.gov.service.notify.NotificationClient
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

@ExtendWith(MockKExtension::class)
@DisplayName("Notification Service tests")
internal class NotificationServiceTest_Generate_Shift_Task {
  private val shiftNotificationRepository: NotificationRepository = mockk(relaxUnitFun = true)
  private val userPreferenceService: UserPreferenceService = mockk(relaxUnitFun = true)
  private val prisonService: PrisonService = mockk(relaxUnitFun = true)
  private val authenticationFacade: AuthenticationFacade = mockk(relaxUnitFun = true)
  private val notifyClient: NotificationClient = mockk(relaxUnitFun = true)
  private val csrClient: CsrClient = mockk(relaxUnitFun = true)
  private val clock = Clock.fixed(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
  private val service = NotificationService(
    shiftNotificationRepository,
    userPreferenceService,
    clock,
    authenticationFacade,
    3,
    notifyClient,
    prisonService,
    csrClient
  )

  @BeforeEach
  fun resetAllMocks() {
    clearMocks(shiftNotificationRepository)
    clearMocks(userPreferenceService)
    clearMocks(notifyClient)
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
        quantumId,
        today.atStartOfDay(),
        shiftType,
        start,
        end,
        task,
        DetailModificationType.ADD
      )

      every { csrClient.getModifiedDetails(any(), any()) } returns listOf(dto1)

      every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(quantumId, start, any(), today.atStartOfDay()) } returns 1

      val results = mutableListOf<Collection<Notification>>()
      every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.refreshNotifications(1)

      assertThat(results[0]).isEqualTo(listOf<Notification>())
    }

    @Test
    fun `Should add multiple Shift Task notifications for one user`() {
      val today = LocalDate.now(clock)
      val quantumId = "CSTRIFE_GEN"
      val start = today.atStartOfDay().plusSeconds(123L)
      val end = today.atStartOfDay().plusSeconds(456L)
      val task = "Guard Duty"
      val dto1 = CsrModifiedDetailDto(
        quantumId,
        today.atStartOfDay(),
        ShiftType.SHIFT,
        start.plusDays(1),
        end,
        task,
        DetailModificationType.EDIT
      )

      val dto2 = CsrModifiedDetailDto(
        quantumId,
        today.atStartOfDay(),
        ShiftType.SHIFT,
        start.plusDays(2),
        end,
        task,
        DetailModificationType.EDIT
      )

      val dto3 = CsrModifiedDetailDto(
        quantumId,
        today.atStartOfDay(),
        ShiftType.SHIFT,
        start.plusDays(3),
        end,
        task,
        DetailModificationType.EDIT
      )

      every { csrClient.getModifiedDetails(any(), any()) } returns listOf(dto1, dto2, dto3)

      every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(quantumId, any(), any(), today.atStartOfDay()) } returns 0

      val results = mutableListOf<Collection<Notification>>()
      every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.refreshNotifications(1)
      val notification1 = Notification(null, quantumId, today.atStartOfDay(), start.plusDays(1), end, task, ShiftType.SHIFT, DetailModificationType.EDIT, false)
      val notification2 = Notification(null, quantumId, today.atStartOfDay(), start.plusDays(2), end, task, ShiftType.SHIFT, DetailModificationType.EDIT, false)
      val notification3 = Notification(null, quantumId, today.atStartOfDay(), start.plusDays(3), end, task, ShiftType.SHIFT, DetailModificationType.EDIT, false)
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
        quantumId,
        today.atStartOfDay(),
        ShiftType.SHIFT,
        start,
        end,
        task,
        DetailModificationType.ADD
      )

      val dto2 = CsrModifiedDetailDto(
        quantumId,
        today.atStartOfDay(),
        ShiftType.SHIFT,
        start,
        end,
        task,
        DetailModificationType.ADD
      )

      val dto3 = CsrModifiedDetailDto(
        quantumId,
        today.atStartOfDay(),
        ShiftType.SHIFT,
        start,
        end,
        task,
        DetailModificationType.ADD
      )

      every { csrClient.getModifiedDetails(any(), any()) } returns listOf(dto1, dto2, dto3)

      every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(quantumId, any(), any(), today.atStartOfDay()) } returns 0

      val results = mutableListOf<Collection<Notification>>()
      every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.refreshNotifications(1)
      val notification1 = Notification(null, quantumId, today.atStartOfDay(), start, end, task, ShiftType.SHIFT, DetailModificationType.ADD, false)
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
        quantumId,
        today.atStartOfDay(),
        ShiftType.OVERTIME,
        start,
        end,
        task,
        DetailModificationType.EDIT
      )

      val dto2 = CsrModifiedDetailDto(
        quantumId,
        today.atStartOfDay(),
        ShiftType.SHIFT,
        start,
        end,
        task,
        DetailModificationType.EDIT
      )

      val dto3 = CsrModifiedDetailDto(
        quantumId,
        today.atStartOfDay(),
        ShiftType.SHIFT,
        start,
        end,
        task,
        DetailModificationType.EDIT
      )

      every { csrClient.getModifiedDetails(any(), any()) } returns listOf(dto1, dto2, dto3)

      every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(quantumId, any(), any(), today.atStartOfDay()) } returns 0

      val results = mutableListOf<Collection<Notification>>()
      every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.refreshNotifications(1)
      val notification1 = Notification(null, quantumId, today.atStartOfDay(), start, end, task, ShiftType.OVERTIME, DetailModificationType.EDIT, false)
      val notification2 = Notification(null, quantumId, today.atStartOfDay(), start, end, task, ShiftType.SHIFT, DetailModificationType.EDIT, false)
      assertThat(results[0]).isEqualTo(listOf(notification1, notification2))
    }

    @Test
    fun `Should add multiple notifications for same shift task with different modified times for one user`() {
      val today = LocalDate.now(clock)
      val quantumId = "CSTRIFE_GEN"
      val start = today.atStartOfDay().plusSeconds(123L)
      val end = today.atStartOfDay().plusSeconds(456L)
      val task = "Guard Duty"
      val dto1 = CsrModifiedDetailDto(
        quantumId,
        today.atStartOfDay(),
        ShiftType.SHIFT,
        start,
        end,
        task,
        DetailModificationType.ADD
      )

      val dto2 = CsrModifiedDetailDto(
        quantumId,
        today.atStartOfDay().plusSeconds(5),
        ShiftType.SHIFT,
        start,
        end,
        task,
        DetailModificationType.ADD
      )

      val dto3 = CsrModifiedDetailDto(
        quantumId,
        today.atStartOfDay().plusSeconds(10),
        ShiftType.SHIFT,
        start,
        end,
        task,
        DetailModificationType.ADD
      )

      every { csrClient.getModifiedDetails(any(), any()) } returns listOf(dto1, dto2, dto3)

      every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(quantumId, any(), any(), today.atStartOfDay()) } returns 0
      every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(quantumId, any(), any(), today.atStartOfDay().plusSeconds(5)) } returns 0
      every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(quantumId, any(), any(), today.atStartOfDay().plusSeconds(10)) } returns 0

      val results = mutableListOf<Collection<Notification>>()
      every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.refreshNotifications(1)
      val notification1 = Notification(null, quantumId, today.atStartOfDay(), start, end, task, ShiftType.SHIFT, DetailModificationType.ADD, false)
      val notification2 = Notification(null, quantumId, today.atStartOfDay().plusSeconds(5), start, end, task, ShiftType.SHIFT, DetailModificationType.ADD, false)
      val notification3 = Notification(null, quantumId, today.atStartOfDay().plusSeconds(10), start, end, task, ShiftType.SHIFT, DetailModificationType.ADD, false)
      assertThat(results[0]).isEqualTo(listOf(notification1, notification2, notification3))
    }

    @Test
    fun `Should save edit Shift Task notification types`() {
      val today = LocalDate.now(clock)
      val quantumId = "CSTRIFE_GEN"
      val shiftDate = today.plusDays(2)
      val start = today.atStartOfDay().plusSeconds(123L)
      val end = today.atStartOfDay().plusSeconds(456L)
      val task = "Guard Duty"
      val dto1 = CsrModifiedDetailDto(
        quantumId,
        today.atStartOfDay(),
        ShiftType.SHIFT,
        start,
        end,
        task,
        DetailModificationType.EDIT
      )

      every { csrClient.getModifiedDetails(any(), any()) } returns listOf(dto1)

      every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(quantumId, start, any(), today.atStartOfDay()) } returns 0

      val results = mutableListOf<Collection<Notification>>()
      every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.refreshNotifications(1)
      val expected = Notification(null, quantumId, today.atStartOfDay(), start, end, task, ShiftType.SHIFT, DetailModificationType.EDIT, false)
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
        quantumId,
        today.atStartOfDay(),
        ShiftType.SHIFT,
        start,
        end,
        task,
        DetailModificationType.ADD
      )

      every { csrClient.getModifiedDetails(any(), any()) } returns listOf(dto1)

      every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(quantumId, start, any(), today.atStartOfDay()) } returns 0

      val results = mutableListOf<Collection<Notification>>()
      every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.refreshNotifications(1)

      val expected = Notification(null, quantumId, today.atStartOfDay(), start, end, task, ShiftType.SHIFT, DetailModificationType.ADD, false)
      assertThat(results[0]).isEqualTo(listOf(expected))
    }

    @Test
    fun `Should save remove Shift Task notification types if not exist in the DB`() {
      val today = LocalDate.now(clock)
      val quantumId = "CSTRIFE_GEN"
      val shiftDate = today.plusDays(2)
      val start = today.atStartOfDay().plusSeconds(123L)
      val end = today.atStartOfDay().plusSeconds(456L)
      val task = "Guard Duty"
      val dto1 = CsrModifiedDetailDto(
        quantumId,
        today.atStartOfDay(),
        ShiftType.SHIFT,
        start,
        end,
        task,
        DetailModificationType.DELETE
      )

      every { csrClient.getModifiedDetails(any(), any()) } returns listOf(dto1)

      every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(quantumId, start, any(), today.atStartOfDay()) } returns 0

      val results = mutableListOf<Collection<Notification>>()
      every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.refreshNotifications(1)

      val expected = Notification(null, quantumId, today.atStartOfDay(), start, end, task, ShiftType.SHIFT, DetailModificationType.DELETE, false)
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
        quantumId,
        today.atStartOfDay(),
        ShiftType.SHIFT,
        start,
        end,
        task,
        DetailModificationType.ADD
      )

      every { csrClient.getModifiedDetails(any(), any()) } returns listOf(dto1)

      every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(quantumId, any(), ShiftType.SHIFT, today.atStartOfDay()) } returns 1

      val results = mutableListOf<Collection<Notification>>()
      every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.refreshNotifications(1)

      assertThat(results[0]).isEqualTo(listOf<Notification>())
    }

    @Test
    fun `Should do nothing if there are no notifications`() {
      every { csrClient.getModifiedDetails(any(), any()) } returns listOf()

      val results = mutableListOf<Collection<Notification>>()
      every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.refreshNotifications(1)

      assertThat(results[0]).hasSize(0)
    }
  }
}
