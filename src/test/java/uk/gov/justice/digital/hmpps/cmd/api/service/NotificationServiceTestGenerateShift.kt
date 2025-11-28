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
import uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.cmd.api.model.Notification
import uk.gov.justice.digital.hmpps.cmd.api.repository.NotificationRepository
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.service.notify.NotificationClient
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@ExtendWith(MockKExtension::class)
@DisplayName("Notification Service tests Generate Shift")
internal class NotificationServiceTestGenerateShift {
  private val notificationRepository: NotificationRepository = mockk(relaxUnitFun = true)
  private val userPreferenceService: UserPreferenceService = mockk(relaxUnitFun = true)
  private val authenticationFacade: HmppsAuthenticationHolder = mockk(relaxUnitFun = true)
  private val notifyClient: NotificationClient = mockk(relaxUnitFun = true)
  private val csrClient: CsrClient = mockk(relaxUnitFun = true)
  private val clock = Clock.fixed(Instant.parse("2022-04-01T10:00:00Z"), ZoneId.systemDefault())
  private val service = NotificationService(
    notificationRepository,
    userPreferenceService,
    clock,
    authenticationFacade,
    3,
    notifyClient,
    csrClient,
    TelemetryClient(),
  )

  private val today = LocalDate.now(clock).atStartOfDay()

  @BeforeEach
  fun resetAllMocks() {
    clearMocks(notificationRepository)
    clearMocks(userPreferenceService)
    clearMocks(notifyClient)
    clearMocks(csrClient)
  }

  @Nested
  @DisplayName("Generate and save Notification tests")
  inner class GenerateAndSaveNotificationTests {

    @Test
    fun `Should disregard Shift Notification if it exists in our db`() {
      val quantumId = "CSTRIFE_GEN"
      val shiftDate = today.plusDays(2).toLocalDate()
      val start = shiftDate.atStartOfDay().plusSeconds(123L)
      val end = shiftDate.atStartOfDay().plusSeconds(456L)
      val task = "Guard Duty"
      val shiftType = ShiftType.SHIFT
      val dto1 = CsrModifiedDetailDto(
        id = 1,
        quantumId = quantumId,
        shiftModified = today,
        shiftType = shiftType,
        detailStart = start,
        detailEnd = end,
        activity = task,
        actionType = DetailModificationType.ADD,
      )

      every { csrClient.getModified(1) } returns listOf(dto1)

      every {
        notificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          start,
          shiftType,
          today,
        )
      } returns 1

      service.getNotifications(1)

      verify(exactly = 0) { notificationRepository.saveAll<Notification>(allAny()) }
    }

    @Test
    fun `Should add multiple Shift notifications for one user`() {
      val quantumId = "CSTRIFE_GEN"
      val start = today.plusSeconds(123L)
      val end = today.plusSeconds(456L)
      val task = "Guard Duty"
      val shiftType = ShiftType.SHIFT
      val dto1 = CsrModifiedDetailDto(
        id = 1,
        quantumId = quantumId,
        shiftModified = today,
        shiftType = shiftType,
        detailStart = start.plusDays(1),
        detailEnd = end.plusDays(1),
        activity = task,
        actionType = DetailModificationType.ADD,
      )
      val dto2 = dto1.copy(id = 2, detailStart = start.plusDays(2), detailEnd = end.plusDays(2))
      val dto3 = dto1.copy(id = 3, detailStart = start.plusDays(3), detailEnd = end.plusDays(3))

      every { csrClient.getModified(any()) } returns listOf(dto1, dto2, dto3)

      every {
        notificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          any(),
          shiftType,
          today,
        )
      } returns 0

      val results = mutableListOf<Collection<Notification>>()
      every { notificationRepository.saveAll(capture(results)) } returns listOf()

      service.getNotifications(1)
      val notification1 = Notification(
        quantumId = quantumId,
        shiftModified = today,
        detailStart = start.plusDays(1),
        detailEnd = end.plusDays(1),
        activity = task,
        parentType = shiftType,
        actionType = DetailModificationType.ADD,
        processed = false,
      )
      val notification2 = notification1.copy(detailStart = start.plusDays(2), detailEnd = end.plusDays(2))
      val notification3 = notification1.copy(detailStart = start.plusDays(3), detailEnd = end.plusDays(3))
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
        1,
        quantumId,
        today,
        shiftType,
        start,
        end,
        task,
        DetailModificationType.ADD,
      )
      val dto2 = dto1.copy(id = 2L)
      val dto3 = dto1.copy(id = 3L)

      every { csrClient.getModified(any()) } returns listOf(dto1, dto2, dto3)

      every {
        notificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          any(),
          shiftType,
          today,
        )
      } returns 0

      val results = mutableListOf<Collection<Notification>>()
      every { notificationRepository.saveAll(capture(results)) } returns listOf()

      service.getNotifications(1)
      val notification1 = Notification(
        id = 0,
        quantumId = quantumId,
        shiftModified = today,
        detailStart = start,
        detailEnd = end,
        activity = task,
        parentType = shiftType,
        actionType = DetailModificationType.ADD,
        processed = false,
      )
      assertThat(results[0]).isEqualTo(listOf(notification1))
    }

    @Test
    fun `Should filter multiple notifications for same shift day with different modified times for one user`() {
      val quantumId = "CSTRIFE_GEN"
      val start = today.plusHours(1L)
      val start2 = today.plusHours(2L)
      val start3 = today.plusHours(3L)
      val end = today.plusHours(12L)
      val task = "Guard Duty"
      val shiftType = ShiftType.SHIFT
      val shiftModified = today
      val shiftModified2 = today.plusSeconds(5)
      val shiftModified3 = today.plusSeconds(10)

      val dto1 =
        CsrModifiedDetailDto(
          id = 1,
          quantumId = quantumId,
          shiftModified = shiftModified,
          shiftType = shiftType,
          detailStart = start,
          detailEnd = end,
          activity = task,
          actionType = DetailModificationType.ADD,
        )
      val dto2 = dto1.copy(id = 2, shiftModified = shiftModified2, detailStart = start2)
      val dto3 = dto1.copy(id = 3, shiftModified = shiftModified3, detailStart = start3)
      val dto4 = dto1.copy(id = 4, actionType = DetailModificationType.EDIT)
      val dto5 = dto4.copy(id = 5, shiftModified = shiftModified2, detailStart = start2)
      val dto6 = dto4.copy(id = 6, shiftModified = shiftModified3, detailStart = start3)
      every { csrClient.getModified(any()) } returns listOf(dto1, dto2, dto3, dto4, dto5, dto6)

      every {
        notificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          start3,
          shiftType,
          shiftModified3,
        )
      } returns 0

      val results = mutableListOf<Collection<Notification>>()
      every { notificationRepository.saveAll(capture(results)) } returns listOf()

      val ids = mutableListOf<List<Long>>()
      every { csrClient.deleteProcessed(1, capture(ids)) }

      service.getNotifications(1)
      val notification1 = Notification(
        quantumId = quantumId,
        shiftModified = shiftModified3,
        detailStart = start3,
        detailEnd = end,
        activity = task,
        parentType = shiftType,
        actionType = DetailModificationType.ADD,
        processed = false,
      )
      val notification2 = notification1.copy(actionType = DetailModificationType.EDIT)
      assertThat(results[0]).isEqualTo(listOf(notification1, notification2))
      assertThat(ids[0]).isEqualTo(listOf(1L, 2L, 3L, 4L, 5L, 6L))
    }

    @Test
    fun `Should not save edit Shift notification types, these are covered by shift Task Notifications`() {
      val quantumId = "CSTRIFE_GEN"
      val task = null
      val shiftType = ShiftType.SHIFT
      val dto1 = CsrModifiedDetailDto(
        id = 1,
        quantumId = quantumId,
        shiftModified = today,
        shiftType = shiftType,
        detailStart = today,
        detailEnd = today,
        activity = task,
        actionType = DetailModificationType.EDIT,
      )

      every { csrClient.getModified(any()) } returns listOf(dto1)

      every {
        notificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndActionType(
          quantumId,
          today,
          shiftType,
          DetailModificationType.ADD,
        )
      } returns 1
      every {
        notificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          today,
          shiftType,
          today,
        )
      } returns 0

      service.getNotifications(1)

      verify(exactly = 0) { notificationRepository.saveAll<Notification>(allAny()) }
    }

    @Test
    fun `Should save Add shift notification types if not exist in the DB`() {
      val quantumId = "CSTRIFE_GEN"
      val task = "Guard Duty"
      val shiftType = ShiftType.SHIFT
      val dto1 = CsrModifiedDetailDto(
        id = 1,
        quantumId = quantumId,
        shiftModified = today,
        shiftType = shiftType,
        detailStart = today,
        detailEnd = today,
        activity = task,
        actionType = DetailModificationType.ADD,
      )

      every { csrClient.getModified(any()) } returns listOf(dto1)

      every {
        notificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          today,
          shiftType,
          today,
        )
      } returns 0

      val results = mutableListOf<Collection<Notification>>()
      every { notificationRepository.saveAll(capture(results)) } returns listOf()
      service.getNotifications(1)

      val expected = Notification(
        quantumId = quantumId,
        shiftModified = today,
        detailStart = today,
        detailEnd = today,
        activity = task,
        parentType = shiftType,
        actionType = DetailModificationType.ADD,
        processed = false,
      )
      assertThat(results[0]).isEqualTo(listOf(expected))
      verify { csrClient.deleteProcessed(1, listOf(1L)) }
    }

    @Test
    fun `Should save remove Shift notification types if not exist in the DB`() {
      val quantumId = "CSTRIFE_GEN"
      val task = "Guard Duty"
      val shiftType = ShiftType.SHIFT
      val dto1 = CsrModifiedDetailDto(
        id = 1,
        quantumId = quantumId,
        shiftModified = today,
        shiftType = shiftType,
        detailStart = today,
        detailEnd = today,
        activity = task,
        actionType = DetailModificationType.DELETE,
      )

      every { csrClient.getModified(any()) } returns listOf(dto1)

      every {
        notificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          today,
          shiftType,
          today,
        )
      } returns 0

      val results = mutableListOf<Collection<Notification>>()
      every { notificationRepository.saveAll(capture(results)) } returns listOf()

      service.getNotifications(1)

      val expected = Notification(
        quantumId = quantumId,
        shiftModified = today,
        detailStart = today,
        detailEnd = today,
        activity = task,
        parentType = shiftType,
        actionType = DetailModificationType.DELETE,
        processed = false,
      )
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
        id = 1,
        quantumId = quantumId,
        shiftModified = today,
        shiftType = shiftType,
        detailStart = start,
        detailEnd = end,
        activity = task,
        actionType = DetailModificationType.ADD,
      )

      every { csrClient.getModified(any()) } returns listOf(dto1)

      every {
        notificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          start,
          shiftType,
          today,
        )
      } returns 1

      service.getNotifications(1)

      verify(exactly = 0) { notificationRepository.saveAll<Notification>(allAny()) }
    }

    @Test
    fun `Should do nothing if there is nothing to do`() {
      every { csrClient.getModified(any()) } returns listOf()

      service.getNotifications(1)

      verify(exactly = 0) { notificationRepository.saveAll<Notification>(allAny()) }
    }

    @Test
    fun `Should Change an Edit with no existing Add in the database to an Add`() {
      val quantumId = "CSTRIFE_GEN"
      val task = null
      val shiftType = ShiftType.SHIFT
      val dto1 = CsrModifiedDetailDto(
        id = 1,
        quantumId = quantumId,
        shiftModified = today,
        shiftType = shiftType,
        detailStart = today,
        detailEnd = today,
        activity = task,
        actionType = DetailModificationType.EDIT,
      )

      every { csrClient.getModified(any()) } returns listOf(dto1, dto1)

      every {
        notificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndActionType(
          quantumId,
          today,
          shiftType,
          DetailModificationType.ADD,
        )
      } returns 0
      every {
        notificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          today,
          shiftType,
          today,
        )
      } returns 0

      val results = mutableListOf<Collection<Notification>>()
      every { notificationRepository.saveAll(capture(results)) } returns listOf()

      service.getNotifications(1)

      val expected = Notification(
        quantumId = quantumId,
        shiftModified = today,
        detailStart = today,
        detailEnd = today,
        activity = task,
        parentType = shiftType,
        actionType = DetailModificationType.ADD,
        processed = false,
      )
      assertThat(results[0]).isEqualTo(listOf(expected))
    }

    @Test
    fun `Should ignore user with changes that are too recent`() {
      val now = LocalDateTime.now(clock)
      val start = today.plusSeconds(123L)
      val end = today.plusSeconds(456L)
      val start2 = today.plusDays(1).plusSeconds(123L)
      val shiftType = ShiftType.SHIFT

      val dto1 = CsrModifiedDetailDto(
        id = 1,
        quantumId = "FINISHED_USER",
        shiftModified = now.minusMinutes(7),
        shiftType = shiftType,
        detailStart = start,
        detailEnd = end,
        activity = "Guard Duty",
        actionType = DetailModificationType.ADD,
      )
      val dto2 = dto1.copy(id = 2, shiftModified = now.minusMinutes(6), quantumId = "STILL_CHANGING_USER")
      val dto3 =
        dto1.copy(
          id = 3,
          shiftModified = now.minusMinutes(6),
          quantumId = "FINISHED_USER",
          activity = "another",
          detailStart = start2,
        )
      val dto4 =
        dto1.copy(id = 4, shiftModified = now.minusMinutes(4), quantumId = "STILL_CHANGING_USER", detailStart = start2)

      every { csrClient.getModified(any()) } returns listOf(dto1, dto2, dto3, dto4)

      every {
        notificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          "FINISHED_USER",
          any(),
          shiftType,
          any(),
        )
      } returns 0

      val results = mutableListOf<Collection<Notification>>()
      every { notificationRepository.saveAll(capture(results)) } returns listOf()

      service.getNotifications(1)

      val notification1 = Notification(
        quantumId = "FINISHED_USER",
        shiftModified = now.minusMinutes(6),
        detailStart = start2,
        detailEnd = end,
        activity = "another",
        parentType = shiftType,
        actionType = DetailModificationType.ADD,
        processed = false,
      )
      val notification2 =
        notification1.copy(shiftModified = now.minusMinutes(7), detailStart = start, activity = "Guard Duty")
      assertThat(results[0]).containsExactly(notification1, notification2)
      verify { csrClient.deleteProcessed(1, listOf(1L, 3L)) }
    }

    @Test
    fun `Should process but not notify when null quantum id or lastmodified`() {
      val quantumId = "CSTRIFE_GEN"
      val shiftDate = today.plusDays(2).toLocalDate()
      val start = shiftDate.atStartOfDay().plusSeconds(123L)
      val end = shiftDate.atStartOfDay().plusSeconds(456L)
      val task = "Guard Duty"
      val shiftType = ShiftType.SHIFT
      val dto1 = CsrModifiedDetailDto(
        id = 1,
        quantumId = quantumId,
        shiftModified = today,
        shiftType = shiftType,
        detailStart = start,
        detailEnd = end,
        activity = task,
        actionType = DetailModificationType.ADD,
      )
      val dto2 = dto1.copy(id = 2, quantumId = null)
      val dto3 = dto1.copy(id = 3, shiftModified = null)
      val dto4 = dto1.copy(id = 4, quantumId = null, shiftModified = null)
      val dto5 = dto1.copy(id = 5)

      every { csrClient.getModified(any()) } returns listOf(dto1, dto2, dto3, dto4, dto5)

      every {
        notificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          start,
          shiftType,
          today,
        )
      } returns 0

      val results = mutableListOf<Collection<Notification>>()
      every { notificationRepository.saveAll(capture(results)) } returns listOf()

      service.getNotifications(1)

      assertThat(results[0]).hasSize(1)
      verify { csrClient.deleteProcessed(1, listOf(1L, 2L, 3L, 4L, 5L)) }
    }
  }
}
