package uk.gov.justice.digital.hmpps.cmd.api.service

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrClient
import uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailModificationType
import uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.cmd.api.model.Notification
import uk.gov.justice.digital.hmpps.cmd.api.model.UserPreference
import uk.gov.justice.digital.hmpps.cmd.api.repository.NotificationRepository
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.service.notify.NotificationClient
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Optional

@DisplayName("Notification Service tests")
internal class NotificationServiceTest {
  private val shiftNotificationRepository: NotificationRepository = mock()
  private val userPreferenceService: UserPreferenceService = mock()
  private val authenticationFacade: HmppsAuthenticationHolder = mock()
  private val notifyClient: NotificationClient = mock()
  private val csrClient: CsrClient = mock()
  private val clock = Clock.fixed(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
  private val service = NotificationService(
    notificationRepository = shiftNotificationRepository,
    userPreferenceService = userPreferenceService,
    clock = clock,
    authenticationFacade = authenticationFacade,
    monthStep = 3,
    notifyClient = notifyClient,
    csrClient = csrClient,
    telemetryClient = TelemetryClient(),
    allowedUsersToNotify = emptySet(),
  )

  private val now = LocalDateTime.now(clock)
  private val from = Optional.of(LocalDate.now(clock).minusDays(1))
  private val to = Optional.of(LocalDate.now(clock).plusDays(1))

  @BeforeEach
  fun resetAllMocks() {
    reset(shiftNotificationRepository, userPreferenceService, notifyClient)
  }

  @Nested
  @DisplayName("Get Notification tests")
  inner class GetNotificationTests {

    @Test
    fun `Should get Notifications`() {
      val quantumId = "XYZ"

      val unprocessedOnly = Optional.of(false)
      val processOnRead = Optional.of(true)

      val shiftNotifications = listOf(getValidNotification(now))
      whenever(shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX))).thenReturn(shiftNotifications)
      whenever(authenticationFacade.username).thenReturn(quantumId)
      whenever(shiftNotificationRepository.saveAll(shiftNotifications)).thenReturn(listOf())
      val returnValue = service.getNotifications(processOnRead, unprocessedOnly, from, to)

      verify(shiftNotificationRepository).findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX))
      verify(shiftNotificationRepository).saveAll(shiftNotifications)
      verifyNoMoreInteractions(shiftNotificationRepository)

      assertThat(returnValue).hasSize(1)
    }

    @Test
    fun `Should filter duplicate notifications Notifications`() {
      val quantumId = "XYZ"
      val unprocessedOnly = Optional.of(false)
      val processOnRead = Optional.of(true)

      val shiftNotifications = listOf(getValidNotification(now), getValidNotification(now), getValidNotification(now))
      whenever(shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX))).thenReturn(shiftNotifications)
      whenever(authenticationFacade.username).thenReturn(quantumId)
      whenever(shiftNotificationRepository.saveAll(shiftNotifications)).thenReturn(listOf())

      val returnValue = service.getNotifications(processOnRead, unprocessedOnly, from, to)

      verify(shiftNotificationRepository).findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX))
      verify(shiftNotificationRepository).saveAll(shiftNotifications)
      verifyNoMoreInteractions(shiftNotificationRepository)

      assertThat(returnValue).hasSize(1)
    }

    @Test
    fun `Should get Notifications when there is only a shift notification`() {
      val quantumId = "XYZ"

      val unprocessedOnly = Optional.of(false)
      val processOnRead = Optional.of(true)

      val shiftNotifications = listOf(getValidNotification(now))
      whenever(shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX))).thenReturn(shiftNotifications)
      whenever(authenticationFacade.username).thenReturn(quantumId)
      whenever(shiftNotificationRepository.saveAll(shiftNotifications)).thenReturn(listOf())

      val returnValue = service.getNotifications(processOnRead, unprocessedOnly, from, to)

      verify(shiftNotificationRepository).findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX))
      verify(shiftNotificationRepository).saveAll(shiftNotifications)
      verifyNoMoreInteractions(shiftNotificationRepository)

      assertThat(returnValue).hasSize(1)
    }

    @Test
    fun `Should not get Notifications when there no notifications`() {
      val quantumId = "XYZ"

      val unprocessedOnly = Optional.of(false)
      val processOnRead = Optional.of(true)

      val shiftNotifications: List<Notification> = listOf()
      whenever(shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX))).thenReturn(shiftNotifications)
      whenever(authenticationFacade.username).thenReturn(quantumId)
      whenever(shiftNotificationRepository.saveAll(shiftNotifications)).thenReturn(listOf())

      val returnValue = service.getNotifications(processOnRead, unprocessedOnly, from, to)

      verify(shiftNotificationRepository).findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX))
      verify(shiftNotificationRepository).saveAll(shiftNotifications)

      verifyNoMoreInteractions(shiftNotificationRepository)

      assertThat(returnValue).hasSize(0)
    }

    @Test
    fun `Should use 'from' and 'to' params`() {
      val quantumId = "XYZ"

      val unprocessedOnly = Optional.of(false)
      val processOnRead = Optional.of(true)

      val shiftNotifications: List<Notification> = listOf()
      whenever(shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX))).thenReturn(shiftNotifications)
      whenever(authenticationFacade.username).thenReturn(quantumId)
      whenever(shiftNotificationRepository.saveAll(shiftNotifications)).thenReturn(listOf())

      service.getNotifications(processOnRead, unprocessedOnly, from, to)

      // Should use the from and to passed in.
      verify(shiftNotificationRepository).findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX))
      verify(shiftNotificationRepository).saveAll(shiftNotifications)
      verifyNoMoreInteractions(shiftNotificationRepository)
    }

    @Test
    fun `Should use defaults if 'from' and 'to' params are empty`() {
      val quantumId = "XYZ"
      val from = Optional.empty<LocalDate>()
      val to = Optional.empty<LocalDate>()
      val unprocessedOnly = Optional.of(false)
      val processOnRead = Optional.of(true)

      // Should use class defaults.
      val defaultFrom = LocalDate.now(clock).withDayOfMonth(1)
      val toDate = defaultFrom.plusMonths(3)
      val defaultTo = toDate.withDayOfMonth(toDate.lengthOfMonth())

      val shiftNotifications: List<Notification> = listOf()
      whenever(shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX))).thenReturn(shiftNotifications)
      whenever(authenticationFacade.username).thenReturn(quantumId)
      whenever(shiftNotificationRepository.saveAll(shiftNotifications)).thenReturn(listOf())

      service.getNotifications(processOnRead, unprocessedOnly, from, to)

      verify(shiftNotificationRepository).findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX))
      verify(shiftNotificationRepository).saveAll(shiftNotifications)

      verifyNoMoreInteractions(shiftNotificationRepository)
    }

    @Test
    fun `Should use default if 'from' param is empty`() {
      val quantumId = "XYZ"
      val from = Optional.empty<LocalDate>()
      val to = Optional.of(LocalDate.now(clock))
      val unprocessedOnly = Optional.of(false)
      val processOnRead = Optional.of(true)

      // Should count back 3 months to create the 'to'.
      val defaultFrom = to.get().minusMonths(3).withDayOfMonth(1)

      val shiftNotifications: List<Notification> = listOf()
      whenever(shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX))).thenReturn(shiftNotifications)
      whenever(authenticationFacade.username).thenReturn(quantumId)
      whenever(shiftNotificationRepository.saveAll(shiftNotifications)).thenReturn(listOf())

      service.getNotifications(processOnRead, unprocessedOnly, from, to)

      verify(shiftNotificationRepository).findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX))
      verify(shiftNotificationRepository).saveAll(shiftNotifications)

      verifyNoMoreInteractions(shiftNotificationRepository)
    }

    @Test
    fun `Should use default if 'to' param is empty`() {
      val quantumId = "XYZ"
      val from = Optional.of(LocalDate.now(clock))
      val to = Optional.empty<LocalDate>()
      val unprocessedOnly = Optional.of(false)
      val processOnRead = Optional.of(true)

      // Should use class defaults.
      val toDate = from.get().plusMonths(3)
      val defaultTo = toDate.withDayOfMonth(toDate.lengthOfMonth())

      val shiftNotifications: List<Notification> = listOf()
      whenever(shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX))).thenReturn(shiftNotifications)
      whenever(authenticationFacade.username).thenReturn(quantumId)
      whenever(shiftNotificationRepository.saveAll(shiftNotifications)).thenReturn(listOf())

      service.getNotifications(processOnRead, unprocessedOnly, from, to)

      verify(shiftNotificationRepository).findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX))
      verify(shiftNotificationRepository).saveAll(shiftNotifications)

      verifyNoMoreInteractions(shiftNotificationRepository)
    }
  }

  @Nested
  @DisplayName("Snooze data specific notify tests")
  inner class SendNotificationWithSnoozeTests {

    @Test
    fun `Should not send a notification if the user has a snooze preference set to future date`() {
      val quantumId1 = "XYZ"
      val shiftNotifications: List<Notification> = listOf(
        Notification(1, quantumId1, now, now, now, null, ShiftType.SHIFT, DetailModificationType.ADD, false),
      )

      val snoozePref = LocalDate.now(clock).plusDays(20)
      whenever(shiftNotificationRepository.findAllByProcessedIsFalse()).thenReturn(shiftNotifications)
      whenever(userPreferenceService.getUserPreference(quantumId1)).thenReturn(UserPreference(quantumId1, snoozePref, "email", "sms", CommunicationPreference.EMAIL))
      whenever(notifyClient.sendEmail(any(), eq("email"), any(), isNull())).thenReturn(null)
      whenever(shiftNotificationRepository.saveAll(shiftNotifications)).thenReturn(shiftNotifications)

      service.sendNotifications()

      verify(shiftNotificationRepository).findAllByProcessedIsFalse()
      verify(userPreferenceService).getUserPreference(quantumId1)
      verify(shiftNotificationRepository).saveAll(shiftNotifications)
    }

    @Test
    fun `Should not send a notification if the user has a snooze preference set to today's date`() {
      val quantumId1 = "XYZ"
      val shiftNotifications: List<Notification> = listOf(
        Notification(1, quantumId1, now, now, now, null, ShiftType.SHIFT, DetailModificationType.ADD, false),
      )

      val snoozePref = LocalDate.now(clock)
      whenever(shiftNotificationRepository.findAllByProcessedIsFalse()).thenReturn(shiftNotifications)
      whenever(userPreferenceService.getUserPreference(quantumId1)).thenReturn(UserPreference(quantumId1, snoozePref, "email", "sms", CommunicationPreference.EMAIL))
      whenever(notifyClient.sendEmail(any(), eq("email"), any(), isNull())).thenReturn(null)
      whenever(shiftNotificationRepository.saveAll(shiftNotifications)).thenReturn(shiftNotifications)

      service.sendNotifications()

      verify(shiftNotificationRepository).findAllByProcessedIsFalse()
      verify(userPreferenceService).getUserPreference(quantumId1)
      verify(shiftNotificationRepository).saveAll(shiftNotifications)
    }

    @Test
    fun `Should send a notification if the user has a snooze preference set to yesterday's date`() {
      val quantumId1 = "XYZ"
      val shiftNotifications: List<Notification> = listOf(
        Notification(1, quantumId1, now, now, now, null, ShiftType.SHIFT, DetailModificationType.ADD, false),
      )

      val snoozePref = LocalDate.now(clock).minusDays(1)
      whenever(shiftNotificationRepository.findAllByProcessedIsFalse()).thenReturn(shiftNotifications)
      whenever(userPreferenceService.getUserPreference(quantumId1)).thenReturn(UserPreference(quantumId1, snoozePref, "email", "sms", CommunicationPreference.EMAIL))
      whenever(notifyClient.sendEmail(any(), eq("email"), any(), isNull())).thenReturn(null)
      whenever(shiftNotificationRepository.saveAll(shiftNotifications)).thenReturn(shiftNotifications)

      service.sendNotifications()

      verify(shiftNotificationRepository).findAllByProcessedIsFalse()
      verify(userPreferenceService).getUserPreference(quantumId1)
      verify(notifyClient).sendEmail(any(), eq("email"), any(), isNull())
      verify(shiftNotificationRepository).saveAll(shiftNotifications)
    }

    @Test
    fun `Should respect communication preferences Email`() {
      val quantumId1 = "XYZ"
      val shiftNotifications: List<Notification> = listOf(
        Notification(1, quantumId1, now.plusDays(4), now.plusDays(4), now.plusDays(4), null, ShiftType.SHIFT, DetailModificationType.ADD, false),
      )

      whenever(shiftNotificationRepository.findAllByProcessedIsFalse()).thenReturn(shiftNotifications)
      whenever(userPreferenceService.getUserPreference(quantumId1)).thenReturn(UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL))
      whenever(notifyClient.sendEmail(any(), eq("email"), any(), isNull())).thenReturn(null)
      whenever(shiftNotificationRepository.saveAll(shiftNotifications)).thenReturn(shiftNotifications)

      service.sendNotifications()

      verify(shiftNotificationRepository).findAllByProcessedIsFalse()
      verify(userPreferenceService).getUserPreference(quantumId1)
      verify(notifyClient).sendEmail(any(), eq("email"), any(), isNull())
      verify(shiftNotificationRepository).saveAll(shiftNotifications)
    }

    @Test
    fun `Should respect communication preferences Sms`() {
      val quantumId1 = "XYZ"
      val shiftNotifications: List<Notification> = listOf(
        Notification(1, quantumId1, now.plusDays(4), now.plusDays(4), now.plusDays(4), null, ShiftType.SHIFT, DetailModificationType.ADD, false),
      )

      whenever(shiftNotificationRepository.findAllByProcessedIsFalse()).thenReturn(shiftNotifications)
      whenever(userPreferenceService.getUserPreference(quantumId1)).thenReturn(UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.SMS))
      whenever(notifyClient.sendSms(any(), eq("sms"), any(), isNull())).thenReturn(null)
      whenever(shiftNotificationRepository.saveAll(shiftNotifications)).thenReturn(shiftNotifications)

      service.sendNotifications()

      verify(shiftNotificationRepository).findAllByProcessedIsFalse()
      verify(userPreferenceService).getUserPreference(quantumId1)
      verify(notifyClient).sendSms(any(), eq("sms"), any(), isNull())
      verify(shiftNotificationRepository).saveAll(shiftNotifications)
    }

    @Test
    fun `Should respect communication preferences None`() {
      val quantumId1 = "XYZ"
      val shiftNotifications: List<Notification> = listOf(
        Notification(1, quantumId1, now.plusDays(4), now.plusDays(4), now.plusDays(4), null, ShiftType.SHIFT, DetailModificationType.ADD, false),
      )

      whenever(shiftNotificationRepository.findAllByProcessedIsFalse()).thenReturn(shiftNotifications)
      whenever(userPreferenceService.getUserPreference(quantumId1)).thenReturn(UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.NONE))
      whenever(shiftNotificationRepository.saveAll(shiftNotifications)).thenReturn(shiftNotifications)

      service.sendNotifications()

      verify(shiftNotificationRepository).findAllByProcessedIsFalse()
      verify(userPreferenceService).getUserPreference(quantumId1)
      verify(shiftNotificationRepository).saveAll(shiftNotifications.filter { it.id == 1L })
    }

    @Test
    fun `Tidy notifications`() {
      whenever(shiftNotificationRepository.deleteAllByShiftModifiedBefore(any())).thenReturn(4)

      service.tidyNotification()

      verify(shiftNotificationRepository).deleteAllByShiftModifiedBefore(any())
    }
  }

  companion object {
    fun getValidNotification(now: LocalDateTime): Notification {
      val quantumId = "XYZ"
      val shiftDate = now.plusDays(2).toLocalDate()
      val shiftModified = now.plusDays(3)
      val taskStart = shiftDate.atStartOfDay().plusSeconds(123L)
      val taskEnd = shiftDate.atStartOfDay().plusSeconds(456L)
      val task = "Any Activity"
      val shiftType = ShiftType.SHIFT
      val actionType = DetailModificationType.ADD

      val processed = false

      return Notification(
        1L,
        quantumId,
        shiftModified,
        taskStart,
        taskEnd,
        task,
        shiftType,
        actionType,
        processed,
      )
    }
  }
}
