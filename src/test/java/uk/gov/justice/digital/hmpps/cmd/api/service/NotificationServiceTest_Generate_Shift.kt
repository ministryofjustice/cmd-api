package uk.gov.justice.digital.hmpps.cmd.api.service

import com.microsoft.applicationinsights.TelemetryClient
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
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
import java.time.LocalDateTime
import java.time.ZoneId

@ExtendWith(MockKExtension::class)
@DisplayName("Notification Service tests Generate Shift")
internal class NotificationServiceTest_Generate_Shift {
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
    csrClient,
    TelemetryClient()
  )

  private val today = LocalDate.now(clock).atStartOfDay()

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
    fun `Should disregard Shift Notification if it exists in our db`() {

      val quantumId = "CSTRIFE_GEN"
      val shiftDate = today.plusDays(2).toLocalDate()
      val start = shiftDate.atStartOfDay().plusSeconds(123L)
      val end = shiftDate.atStartOfDay().plusSeconds(456L)
      val task = "Guard Duty"
      val shiftType = ShiftType.SHIFT
      val dto1 = CsrModifiedDetailDto(
        quantumId = quantumId,
        shiftModified = today,
        shiftType = shiftType,
        detailStart = start,
        detailEnd = end,
        activity = task,
        actionType = DetailModificationType.ADD
      )

      every { csrClient.getModifiedShifts("Main Gate", 1) } returns listOf()
      every { csrClient.getModifiedDetails("Main Gate", 1) } returns listOf(dto1)

      every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(quantumId, start, shiftType, today) } returns 1

      val results = mutableListOf<Collection<Notification>>()
      every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.refreshNotifications(1)

      assertThat(results[0]).isEqualTo(listOf<Notification>())
    }

    @Test
    fun `Should add multiple Shift notifications for one user`() {

      val quantumId = "CSTRIFE_GEN"
      val start = today.plusSeconds(123L)
      val end = today.plusSeconds(456L)
      val task = "Guard Duty"
      val shiftType = ShiftType.SHIFT
      val dto1 = CsrModifiedDetailDto(
        null,
        quantumId,
        today,
        shiftType,
        start.plusDays(1),
        end.plusDays(1),
        task,
        DetailModificationType.ADD
      )

      val dto2 = CsrModifiedDetailDto(
        null,
        quantumId,
        today,
        shiftType,
        start.plusDays(2),
        end.plusDays(2),
        task,
        DetailModificationType.ADD
      )

      val dto3 = CsrModifiedDetailDto(
        null,
        quantumId,
        today,
        shiftType,
        start.plusDays(3),
        end.plusDays(3),
        task,
        DetailModificationType.ADD
      )

      every { csrClient.getModifiedShifts(any(), any()) } returns listOf(dto1, dto2, dto3)
      every { csrClient.getModifiedDetails(any(), any()) } returns listOf()

      every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(quantumId, any(), shiftType, today) } returns 0

      val results = mutableListOf<Collection<Notification>>()
      every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.refreshNotifications(1)
      val notification1 = Notification(0, quantumId, today, start.plusDays(1), end.plusDays(1), task, shiftType, DetailModificationType.ADD, false)
      val notification2 = Notification(0, quantumId, today, start.plusDays(2), end.plusDays(2), task, shiftType, DetailModificationType.ADD, false)
      val notification3 = Notification(0, quantumId, today, start.plusDays(3), end.plusDays(3), task, shiftType, DetailModificationType.ADD, false)
      assertThat(results[0]).isEqualTo(listOf(notification1, notification2, notification3))
    }

    @Test
    fun `Should not add multiple duplicate Shift notifications for one user`() {

      val quantumId = "CSTRIFE_GEN"
      val start = LocalDateTime.now(clock)
      val end = LocalDateTime.now(clock)
      val task = "Guard Duty"
      val shiftType = ShiftType.SHIFT
      val dto1 = CsrModifiedDetailDto(
        null,
        quantumId,
        today,
        shiftType,
        start,
        end,
        task,
        DetailModificationType.ADD
      )

      val dto2 = CsrModifiedDetailDto(
        null,
        quantumId,
        today,
        shiftType,
        start,
        end,
        task,
        DetailModificationType.ADD
      )

      val dto3 = CsrModifiedDetailDto(
        null,
        quantumId,
        today,
        shiftType,
        start,
        end,
        task,
        DetailModificationType.ADD
      )

      every { csrClient.getModifiedShifts(any(), any()) } returns listOf(dto1, dto2, dto3)
      every { csrClient.getModifiedDetails(any(), any()) } returns listOf()

      every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(quantumId, any(), shiftType, today) } returns 0

      val results = mutableListOf<Collection<Notification>>()
      every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.refreshNotifications(1)
      val notification1 = Notification(0, quantumId, today, start, end, task, shiftType, DetailModificationType.ADD, false)
      assertThat(results[0]).isEqualTo(listOf(notification1))
    }

    @Test
    fun `Should add multiple notifications for same shift with different modified times for one user`() {

      val quantumId = "CSTRIFE_GEN"
      val start = today.plusSeconds(123L)
      val end = today.plusSeconds(456L)
      val task = "Guard Duty"
      val shiftType = ShiftType.SHIFT
      val dto1 = CsrModifiedDetailDto(
        null,
        quantumId,
        today,
        shiftType,
        start,
        end,
        task,
        DetailModificationType.ADD
      )

      val dto2 = CsrModifiedDetailDto(
        null,
        quantumId,
        today.plusSeconds(5),
        shiftType,
        start,
        end,
        task,
        DetailModificationType.ADD
      )

      val dto3 = CsrModifiedDetailDto(
        null,
        quantumId,
        today.plusSeconds(10),
        shiftType,
        start,
        end,
        task,
        DetailModificationType.ADD
      )

      every { csrClient.getModifiedShifts(any(), any()) } returns listOf()
      every { csrClient.getModifiedDetails(any(), any()) } returns listOf(dto1, dto2, dto3)

      every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(quantumId, any(), shiftType, today) } returns 0
      every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(quantumId, any(), shiftType, today.plusSeconds(5)) } returns 0
      every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(quantumId, any(), shiftType, today.plusSeconds(10)) } returns 0

      val results = mutableListOf<Collection<Notification>>()
      every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.refreshNotifications(1)
      val notification1 = Notification(0, quantumId, today, start, end, task, shiftType, DetailModificationType.ADD, false)
      val notification2 = Notification(0, quantumId, today.plusSeconds(5), start, end, task, shiftType, DetailModificationType.ADD, false)
      val notification3 = Notification(0, quantumId, today.plusSeconds(10), start, end, task, shiftType, DetailModificationType.ADD, false)
      assertThat(results[0]).isEqualTo(listOf(notification1, notification2, notification3))
    }

    @Test
    fun `Should not save edit Shift notification types, these are covered by shift Task Notifications`() {

      val quantumId = "CSTRIFE_GEN"
      val task = null
      val shiftType = ShiftType.SHIFT
      val dto1 = CsrModifiedDetailDto(
        null,
        quantumId,
        today,
        shiftType,
        today,
        today,
        task,
        DetailModificationType.EDIT
      )

      every { csrClient.getModifiedShifts(any(), any()) } returns listOf()
      every { csrClient.getModifiedDetails(any(), any()) } returns listOf(dto1)

      every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndActionType(quantumId, today, shiftType, DetailModificationType.ADD) } returns 1
      every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(quantumId, today, shiftType, today) } returns 0

      val results = mutableListOf<Collection<Notification>>()
      every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.refreshNotifications(1)

      assertThat(results[0]).isEqualTo(listOf<Notification>())
    }

    @Test
    fun `Should save Add shift notification types if not exist in the DB`() {

      val quantumId = "CSTRIFE_GEN"
      val task = "Guard Duty"
      val shiftType = ShiftType.SHIFT
      val dto1 = CsrModifiedDetailDto(
        null,
        quantumId,
        today,
        shiftType,
        today,
        today,
        task,
        DetailModificationType.ADD
      )

      every { csrClient.getModifiedShifts(any(), any()) } returns listOf()
      every { csrClient.getModifiedDetails(any(), any()) } returns listOf(dto1)

      every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(quantumId, today, shiftType, today) } returns 0

      val results = mutableListOf<Collection<Notification>>()
      every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()
      service.refreshNotifications(1)

      val expected = Notification(0, quantumId, today, today, today, task, shiftType, DetailModificationType.ADD, false)
      assertThat(results[0]).isEqualTo(listOf(expected))
    }

    @Test
    fun `Should save remove Shift notification types if not exist in the DB`() {
      val quantumId = "CSTRIFE_GEN"
      val task = "Guard Duty"
      val shiftType = ShiftType.SHIFT
      val dto1 = CsrModifiedDetailDto(
        null,
        quantumId,
        today,
        shiftType,
        today,
        today,
        task,
        DetailModificationType.DELETE
      )

      every { csrClient.getModifiedShifts(any(), any()) } returns listOf()
      every { csrClient.getModifiedDetails(any(), any()) } returns listOf(dto1)

      every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(quantumId, today, shiftType, today) } returns 0

      val results = mutableListOf<Collection<Notification>>()
      every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.refreshNotifications(1)

      val expected = Notification(0, quantumId, today, today, today, task, shiftType, DetailModificationType.DELETE, false)
      assertThat(results[0]).isEqualTo(listOf(expected))
    }

    @Test
    fun `Should disregard unprocessed Shift Notification duplicates`() {

      val quantumId = "CSTRIFE_GEN"
      val shiftDate = today.plusDays(2).toLocalDate()
      val start = shiftDate.atStartOfDay().plusSeconds(123L)
      val end = shiftDate.atStartOfDay().plusSeconds(456L)
      val task = "Guard Duty"
      val shiftType = ShiftType.SHIFT
      val dto1 = CsrModifiedDetailDto(
        null,
        quantumId,
        today,
        shiftType,
        start,
        end,
        task,
        DetailModificationType.ADD
      )

      every { csrClient.getModifiedShifts(any(), any()) } returns listOf()
      every { csrClient.getModifiedDetails(any(), any()) } returns listOf(dto1)

      every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(quantumId, start, shiftType, today) } returns 1

      val results = mutableListOf<Collection<Notification>>()
      every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.refreshNotifications(1)

      assertThat(results[0]).isEqualTo(listOf<Notification>())
    }

    @Test
    fun `Should do nothing if there is nothing to do`() {
      every { csrClient.getModifiedShifts(any(), any()) } returns listOf()
      every { csrClient.getModifiedDetails(any(), any()) } returns listOf()

      val slot = slot<Collection<Notification>>()
      every { shiftNotificationRepository.saveAll(capture(slot)) } returns listOf()

      service.refreshNotifications(1)

      assertThat(slot.captured).isEqualTo(listOf<Notification>())
    }

    @Test
    fun `Should Change an Edit with no existing Add in the database to an Edit`() {

      val quantumId = "CSTRIFE_GEN"
      val task = null
      val shiftType = ShiftType.SHIFT
      val dto1 = CsrModifiedDetailDto(
        null,
        quantumId,
        today,
        shiftType,
        today,
        today,
        task,
        DetailModificationType.EDIT
      )

      every { csrClient.getModifiedShifts(any(), any()) } returns listOf(dto1)
      every { csrClient.getModifiedDetails(any(), any()) } returns listOf(dto1)

      every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndActionType(quantumId, today, shiftType, DetailModificationType.ADD) } returns 0
      every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(quantumId, today, shiftType, today) } returns 0

      val results = mutableListOf<Collection<Notification>>()
      every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.refreshNotifications(1)

      val expected = Notification(0, quantumId, today, today, today, task, shiftType, DetailModificationType.ADD, false)
      assertThat(results[0]).isEqualTo(listOf(expected))
    }

    @Test
    fun `Should handle timeout exceptions`() {

      val quantumId = "CSTRIFE_GEN"
      val shiftDate = today.toLocalDate()
      val start = shiftDate.atStartOfDay()
      val end = shiftDate.atStartOfDay()
      val task = "Guard Duty"
      val shiftType = ShiftType.SHIFT
      val dto1 = CsrModifiedDetailDto(
        null,
        quantumId,
        today,
        shiftType,
        start,
        end,
        task,
        DetailModificationType.ADD
      )

      every { csrClient.getModifiedShifts("Main Gate", 1) } throws RuntimeException("test")
      every { csrClient.getModifiedDetails("Main Gate", 1) } returns listOf(dto1)

      every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(quantumId, start, shiftType, today) } returns 1

      val results = mutableListOf<Collection<Notification>>()
      every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.refreshNotifications(1)

      assertThat(results[0]).isEqualTo(listOf<Notification>())
    }
  }
}
