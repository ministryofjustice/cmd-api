package uk.gov.justice.digital.hmpps.cmd.api.service

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
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
import java.time.ZoneId

@DisplayName("Notification Service tests Generate Shift task")
internal class NotificationServiceTestGenerateShiftTask {
  private val notificationRepository: NotificationRepository = mock()
  private val userPreferenceService: UserPreferenceService = mock()
  private val authenticationFacade: HmppsAuthenticationHolder = mock()
  private val notifyClient: NotificationClient = mock()
  private val csrClient: CsrClient = mock()
  private val clock = Clock.fixed(Instant.parse("2022-04-01T10:00:00Z"), ZoneId.systemDefault())
  private val service = NotificationService(
    notificationRepository = notificationRepository,
    userPreferenceService = userPreferenceService,
    clock = clock,
    authenticationFacade = authenticationFacade,
    monthStep = 3,
    notifyClient = notifyClient,
    csrClient = csrClient,
    telemetryClient = TelemetryClient(),
    allowedUsersToNotify = emptySet(),
  )

  @BeforeEach
  fun resetAllMocks() {
    reset(notificationRepository, userPreferenceService, notifyClient, csrClient)
  }

  @Nested
  @DisplayName("Generate and save Notification tests")
  inner class GenerateAndSaveNotificationTests {

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
        DetailModificationType.ADD,
      )
      whenever(csrClient.getModified(any())).thenReturn(listOf(dto1))

      whenever(
        notificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          eq(quantumId),
          eq(start),
          any(),
          eq(today.atStartOfDay()),
        ),
      ).thenReturn(1)

      service.getNotifications(1)

      verify(notificationRepository, times(0)).saveAll<Notification>(any())
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
        start,
        end,
        task,
        DetailModificationType.EDIT,
      )

      val dto2 = CsrModifiedDetailDto(
        2,
        quantumId,
        today.atStartOfDay(),
        ShiftType.SHIFT,
        start.plusDays(1),
        end,
        task,
        DetailModificationType.EDIT,
      )

      val dto3 = CsrModifiedDetailDto(
        3,
        quantumId,
        today.atStartOfDay(),
        ShiftType.SHIFT,
        start.plusDays(2),
        end,
        task,
        DetailModificationType.EDIT,
      )

      whenever(csrClient.getModified(any())).thenReturn(listOf(dto1, dto2, dto3))

      whenever(
        notificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          eq(quantumId),
          any(),
          any(),
          eq(today.atStartOfDay()),
        ),
      ).thenReturn(0)

      val results = argumentCaptor<Collection<Notification>>()
      whenever(notificationRepository.saveAll(results.capture())).thenReturn(listOf())

      service.getNotifications(1)

      val notification1 = Notification(
        0,
        quantumId,
        today.atStartOfDay(),
        start,
        end,
        task,
        ShiftType.SHIFT,
        DetailModificationType.EDIT,
        false,
      )
      val notification2 = Notification(
        0,
        quantumId,
        today.atStartOfDay(),
        start.plusDays(1),
        end,
        task,
        ShiftType.SHIFT,
        DetailModificationType.EDIT,
        false,
      )
      val notification3 = Notification(
        0,
        quantumId,
        today.atStartOfDay(),
        start.plusDays(2),
        end,
        task,
        ShiftType.SHIFT,
        DetailModificationType.EDIT,
        false,
      )
      assertThat(results.firstValue).isEqualTo(listOf(notification1, notification2, notification3))
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
        DetailModificationType.ADD,
      )
      val dto2 = dto1.copy(id = 2)
      val dto3 = dto1.copy(id = 3)

      whenever(csrClient.getModified(any())).thenReturn(listOf(dto1, dto2, dto3))

      whenever(
        notificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          eq(quantumId),
          any(),
          any(),
          eq(today.atStartOfDay()),
        ),
      ).thenReturn(0)

      val results = argumentCaptor<Collection<Notification>>()
      whenever(notificationRepository.saveAll(results.capture())).thenReturn(listOf())

      service.getNotifications(1)
      val notification1 = Notification(
        0,
        quantumId,
        today.atStartOfDay(),
        start,
        end,
        task,
        ShiftType.SHIFT,
        DetailModificationType.ADD,
        false,
      )
      assertThat(results.firstValue).isEqualTo(listOf(notification1))
    }

    @Test
    fun `Should filter out duplicate Shift Task notifications when there is an OverTime one too`() {
      val today = LocalDate.now(clock)
      val quantumId = "CSTRIFE_GEN"
      val start = today.atStartOfDay().plusSeconds(123L)
      val end = today.atStartOfDay().plusSeconds(456L)
      val task = "Guard Duty"
      val dto1 = CsrModifiedDetailDto(
        1,
        quantumId,
        today.atStartOfDay(),
        ShiftType.OVERTIME,
        start,
        end,
        task,
        DetailModificationType.EDIT,
      )
      val dto2 = dto1.copy(id = 2, shiftType = ShiftType.SHIFT)
      val dto3 = dto1.copy(id = 3, shiftType = ShiftType.SHIFT)

      whenever(csrClient.getModified(any())).thenReturn(listOf(dto2, dto3, dto1))

      whenever(
        notificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          eq(quantumId),
          any(),
          any(),
          eq(today.atStartOfDay()),
        ),
      ).thenReturn(0)

      val results = argumentCaptor<Collection<Notification>>()
      whenever(notificationRepository.saveAll(results.capture())).thenReturn(listOf())

      service.getNotifications(1)
      val notification1 = Notification(
        0,
        quantumId,
        today.atStartOfDay(),
        start,
        end,
        task,
        ShiftType.SHIFT,
        DetailModificationType.EDIT,
        false,
      )
      val notification2 = Notification(
        0,
        quantumId,
        today.atStartOfDay(),
        start,
        end,
        task,
        ShiftType.OVERTIME,
        DetailModificationType.EDIT,
        false,
      )
      assertThat(results.firstValue).isEqualTo(listOf(notification1, notification2))
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
        DetailModificationType.ADD,
      )
      val dto2 = dto1.copy(id = 2, shiftModified = today.atStartOfDay().plusSeconds(5))
      val dto3 = dto1.copy(id = 3, shiftModified = today.atStartOfDay().plusSeconds(10))

      whenever(csrClient.getModified(any())).thenReturn(listOf(dto1, dto2, dto3))

      whenever(
        notificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          eq(quantumId),
          any(),
          any(),
          eq(today.atStartOfDay().plusSeconds(10)),
        ),
      ).thenReturn(0)

      val results = argumentCaptor<Collection<Notification>>()
      whenever(notificationRepository.saveAll(results.capture())).thenReturn(listOf())

      service.getNotifications(1)
      val notification1 = Notification(
        0,
        quantumId,
        today.atStartOfDay().plusSeconds(10),
        start,
        end,
        task,
        ShiftType.SHIFT,
        DetailModificationType.ADD,
        false,
      )

      assertThat(results.firstValue).isEqualTo(listOf(notification1))
    }

    @Test
    fun `Should save edit Shift Task notification types`() {
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
        DetailModificationType.EDIT,
      )

      whenever(csrClient.getModified(any())).thenReturn(listOf(dto1))

      whenever(
        notificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          eq(quantumId),
          eq(start),
          any(),
          eq(today.atStartOfDay()),
        ),
      ).thenReturn(0)

      val results = argumentCaptor<Collection<Notification>>()
      whenever(notificationRepository.saveAll(results.capture())).thenReturn(listOf())

      service.getNotifications(1)
      val expected = Notification(
        0,
        quantumId,
        today.atStartOfDay(),
        start,
        end,
        task,
        ShiftType.SHIFT,
        DetailModificationType.EDIT,
        false,
      )
      assertThat(results.firstValue).isEqualTo(listOf(expected))
    }

    @Test
    fun `Should save Add shift Task notification types if not exist in the DB`() {
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
        DetailModificationType.ADD,
      )

      whenever(csrClient.getModified(any())).thenReturn(listOf(dto1))

      whenever(
        notificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          eq(quantumId),
          eq(start),
          any(),
          eq(today.atStartOfDay()),
        ),
      ).thenReturn(0)

      val results = argumentCaptor<Collection<Notification>>()
      whenever(notificationRepository.saveAll(results.capture())).thenReturn(listOf())

      service.getNotifications(1)

      val expected = Notification(
        0,
        quantumId,
        today.atStartOfDay(),
        start,
        end,
        task,
        ShiftType.SHIFT,
        DetailModificationType.ADD,
        false,
      )
      assertThat(results.firstValue).isEqualTo(listOf(expected))
    }

    @Test
    fun `Should save remove Shift Task notification types if not exist in the DB`() {
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
        DetailModificationType.DELETE,
      )

      whenever(csrClient.getModified(any())).thenReturn(listOf(dto1))

      whenever(
        notificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          eq(quantumId),
          eq(start),
          any(),
          eq(today.atStartOfDay()),
        ),
      ).thenReturn(0)

      val results = argumentCaptor<Collection<Notification>>()
      whenever(notificationRepository.saveAll(results.capture())).thenReturn(listOf())

      service.getNotifications(1)

      val expected = Notification(
        0,
        quantumId,
        today.atStartOfDay(),
        start,
        end,
        task,
        ShiftType.SHIFT,
        DetailModificationType.DELETE,
        false,
      )
      assertThat(results.firstValue).isEqualTo(listOf(expected))
    }

    @Test
    fun `Should disregard unprocessed Shift Task Notification duplicates`() {
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
        DetailModificationType.ADD,
      )
      whenever(csrClient.getModified(any())).thenReturn(listOf(dto1))

      whenever(
        notificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          eq(quantumId),
          any(),
          eq(ShiftType.SHIFT),
          eq(today.atStartOfDay()),
        ),
      ).thenReturn(1)

      service.getNotifications(1)

      verify(notificationRepository, times(0)).saveAll<Notification>(any())
    }

    @Test
    fun `Should do nothing if there are no notifications`() {
      whenever(csrClient.getModified(any())).thenReturn(listOf())

      service.getNotifications(1)

      verify(notificationRepository, times(0)).saveAll<Notification>(any())
    }
  }
}
