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
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
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
import uk.gov.service.notify.NotificationClientException
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@DisplayName("Notification Service tests - send")
internal class NotificationServiceTestSend {
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

  @BeforeEach
  fun resetAllMocks() {
    reset(shiftNotificationRepository, userPreferenceService, notifyClient)
  }

  @Nested
  @DisplayName("Send Notification tests")
  inner class SendNotificationTests {

    @Test
    fun `Should do nothing if there are no notifications`() {
      val shiftNotifications: List<Notification> = listOf()

      whenever(shiftNotificationRepository.findAllByProcessedIsFalse()).thenReturn(shiftNotifications)

      service.sendNotifications()

      verify(shiftNotificationRepository).findAllByProcessedIsFalse()
    }

    @Test
    fun `Should do nothing if user has no preferences`() {
      val quantumId1 = "XYZ"
      val shiftNotifications: List<Notification> = listOf(
        Notification(1, quantumId1, now, now, now, null, ShiftType.SHIFT, DetailModificationType.ADD, false),
      )
      whenever(shiftNotificationRepository.findAllByProcessedIsFalse()).thenReturn(shiftNotifications)
      whenever(shiftNotificationRepository.saveAll(shiftNotifications)).thenReturn(shiftNotifications)
      whenever(userPreferenceService.getUserPreference(quantumId1)).thenReturn(null)

      service.sendNotifications()

      verify(shiftNotificationRepository).findAllByProcessedIsFalse()
      verify(shiftNotificationRepository).saveAll(shiftNotifications)
      verify(userPreferenceService).getUserPreference(quantumId1)
      verifyNoMoreInteractions(notifyClient)
    }

    @Test
    fun `Should combine notifications to one user`() {
      val quantumId1 = "XYZ"
      val shiftNotifications: List<Notification> = listOf(
        Notification(1, quantumId1, now.plusDays(4), now.plusDays(4), now.plusDays(4), null, ShiftType.SHIFT, DetailModificationType.ADD, false),
        Notification(2, quantumId1, now.plusDays(5), now.plusDays(4), now.plusDays(4), null, ShiftType.SHIFT, DetailModificationType.ADD, false),
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
    fun `Should send a notification to one user`() {
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
    fun `Should send notifications to two users`() {
      val quantumId1 = "XYZ"
      val quantumId2 = "ABC"
      val shiftNotifications: List<Notification> = listOf(
        Notification(1, quantumId1, now.plusDays(4), now.plusDays(4), now.plusDays(4), null, ShiftType.SHIFT, DetailModificationType.ADD, false),
        Notification(2, quantumId2, now.plusDays(5), now.plusDays(5), now.plusDays(5), null, ShiftType.SHIFT, DetailModificationType.ADD, false),
      )

      whenever(shiftNotificationRepository.findAllByProcessedIsFalse()).thenReturn(shiftNotifications)
      whenever(userPreferenceService.getUserPreference(quantumId1)).thenReturn(UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL))
      whenever(userPreferenceService.getUserPreference(quantumId2)).thenReturn(UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL))
      whenever(notifyClient.sendEmail(any(), eq("email"), any(), isNull())).thenReturn(null)
      whenever(shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 1L })).thenReturn(shiftNotifications)
      whenever(shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 2L })).thenReturn(shiftNotifications)

      service.sendNotifications()

      verify(shiftNotificationRepository).findAllByProcessedIsFalse()
      verify(userPreferenceService).getUserPreference(quantumId1)
      verify(userPreferenceService).getUserPreference(quantumId2)
      verify(notifyClient, times(2)).sendEmail(any(), eq("email"), any(), isNull())
      verify(shiftNotificationRepository).saveAll(shiftNotifications.filter { it.id == 1L })
    }

    @Test
    fun `Should send notifications to two users with different preferences`() {
      val quantumId1 = "XYZ"
      val quantumId2 = "ABC"
      val shiftNotifications: List<Notification> = listOf(
        Notification(1, quantumId1, now.plusDays(4), now.plusDays(4), now.plusDays(4), null, ShiftType.SHIFT, DetailModificationType.ADD, false),
        Notification(2, quantumId2, now.plusDays(5), now.plusDays(5), now.plusDays(5), null, ShiftType.SHIFT, DetailModificationType.ADD, false),
      )

      whenever(shiftNotificationRepository.findAllByProcessedIsFalse()).thenReturn(shiftNotifications)
      whenever(userPreferenceService.getUserPreference(quantumId1)).thenReturn(UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL))
      whenever(userPreferenceService.getUserPreference(quantumId2)).thenReturn(UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.SMS))
      whenever(notifyClient.sendEmail(any(), eq("email"), any(), isNull())).thenReturn(null)
      whenever(notifyClient.sendSms(any(), eq("sms"), any(), isNull())).thenReturn(null)
      whenever(shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 1L })).thenReturn(shiftNotifications)
      whenever(shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 2L })).thenReturn(shiftNotifications)

      service.sendNotifications()

      verify(shiftNotificationRepository).findAllByProcessedIsFalse()
      verify(userPreferenceService).getUserPreference(quantumId1)
      verify(userPreferenceService).getUserPreference(quantumId2)
      verify(notifyClient).sendEmail(any(), eq("email"), any(), isNull())
      verify(notifyClient).sendSms(any(), eq("sms"), any(), isNull())
      verify(shiftNotificationRepository).saveAll(shiftNotifications.filter { it.id == 1L })
    }

    @Test
    fun `Should send notifications to two users with different preferences when the third one is 'NONE'`() {
      val quantumId1 = "XYZ"
      val quantumId2 = "ABC"
      val quantumId3 = "123"

      val shiftNotifications: List<Notification> = listOf(
        Notification(1, quantumId1, now.plusDays(4), now.plusDays(4), now.plusDays(4), null, ShiftType.SHIFT, DetailModificationType.ADD, false),
        Notification(2, quantumId2, now.plusDays(5), now.plusDays(5), now.plusDays(5), null, ShiftType.SHIFT, DetailModificationType.ADD, false),
        Notification(3, quantumId3, now.plusDays(5), now.plusDays(5), now.plusDays(5), null, ShiftType.SHIFT, DetailModificationType.ADD, false),
      )

      whenever(shiftNotificationRepository.findAllByProcessedIsFalse()).thenReturn(shiftNotifications)
      whenever(userPreferenceService.getUserPreference(quantumId1)).thenReturn(UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL))
      whenever(userPreferenceService.getUserPreference(quantumId2)).thenReturn(UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.SMS))
      whenever(userPreferenceService.getUserPreference(quantumId3)).thenReturn(UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.NONE))
      whenever(notifyClient.sendEmail(any(), eq("email"), any(), isNull())).thenReturn(null)
      whenever(notifyClient.sendSms(any(), eq("sms"), any(), isNull())).thenReturn(null)
      whenever(shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 1L })).thenReturn(shiftNotifications)
      whenever(shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 2L })).thenReturn(shiftNotifications)
      whenever(shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 3L })).thenReturn(shiftNotifications)

      service.sendNotifications()

      verify(shiftNotificationRepository).findAllByProcessedIsFalse()
      verify(userPreferenceService).getUserPreference(quantumId1)
      verify(userPreferenceService).getUserPreference(quantumId2)
      verify(userPreferenceService).getUserPreference(quantumId3)
      verify(notifyClient).sendEmail(any(), eq("email"), any(), isNull())
      verify(notifyClient).sendSms(any(), eq("sms"), any(), isNull())
      verify(shiftNotificationRepository).saveAll(shiftNotifications.filter { it.id == 1L })
      verify(shiftNotificationRepository).saveAll(shiftNotifications.filter { it.id == 2L })
      verify(shiftNotificationRepository).saveAll(shiftNotifications.filter { it.id == 3L })
    }

    @Test
    fun `Should order events by shiftDate then StartTime`() {
      val frozenClock = Clock.fixed(LocalDate.of(2010, 10, 29).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
      val quantumId = "CSTRIFE_GEN"
      val date = LocalDate.now(frozenClock).plusDays(4).atStartOfDay()
      val modified1 = date.plusHours(1)

      val shiftNotifications: List<Notification> = listOf(
        Notification(1, quantumId, modified1, date.plusSeconds(0), date.plusSeconds(0), null, ShiftType.SHIFT, DetailModificationType.ADD, false),
        Notification(1, quantumId, modified1, date.plusSeconds(1234), date.plusSeconds(4567), "A Task", ShiftType.SHIFT, DetailModificationType.ADD, false),
        Notification(1, quantumId, modified1, date.plusSeconds(9876), date.plusSeconds(6544), "Any Task", ShiftType.SHIFT, DetailModificationType.ADD, false),
        Notification(1, quantumId, modified1, date.plusSeconds(1234).minusDays(1), date.plusSeconds(4567), "Any Task", ShiftType.SHIFT, DetailModificationType.ADD, false),
      )

      whenever(shiftNotificationRepository.findAllByProcessedIsFalse()).thenReturn(shiftNotifications)
      whenever(userPreferenceService.getUserPreference(quantumId)).thenReturn(UserPreference(quantumId, null, "email", "sms", CommunicationPreference.EMAIL))
      whenever(notifyClient.sendEmail(any(), eq("email"), any(), isNull())).thenReturn(null)
      whenever(notifyClient.sendSms(any(), eq("sms"), any(), isNull())).thenReturn(null)
      whenever(shiftNotificationRepository.saveAll(shiftNotifications)).thenReturn(shiftNotifications)

      val slot = argumentCaptor<Map<String, String>>()
      whenever(notifyClient.sendEmail(any(), any(), slot.capture(), isNull())).thenReturn(null)

      service.sendNotifications()

      verify(shiftNotificationRepository).findAllByProcessedIsFalse()
      verify(userPreferenceService).getUserPreference(quantumId)
      verify(notifyClient).sendEmail(any(), eq("email"), any(), isNull())
      verify(notifyClient, times(0)).sendSms(any(), eq("sms"), any(), isNull())
      verify(shiftNotificationRepository).saveAll(shiftNotifications)

      assertThat(slot.firstValue.getValue("not1")).isEqualTo("* Your detail on Monday, 1st November (00:20:34 - 01:16:07) has been added.")
      assertThat(slot.firstValue.getValue("not2")).isEqualTo("* Your shift on Tuesday, 2nd November has been added.")
      assertThat(slot.firstValue.getValue("not3")).isEqualTo("* Your detail on Tuesday, 2nd November (00:20:34 - 01:16:07) has been added.")
      assertThat(slot.firstValue.getValue("not4")).isEqualTo("* Your detail on Tuesday, 2nd November (02:44:36 - 01:49:04) has been added.")
    }

    @Test
    fun `Should handle notify exceptions`() {
      val quantumId = "CSTRIFE_GEN"
      val date = LocalDate.now().plusDays(4).atStartOfDay()
      val modified1 = date.plusHours(1)

      val shiftNotifications: List<Notification> = listOf(
        Notification(1, quantumId, modified1, date, date, null, ShiftType.SHIFT, DetailModificationType.ADD, false),
        Notification(2, quantumId, modified1, date, date, "A Task", ShiftType.SHIFT, DetailModificationType.ADD, false),
      )

      whenever(shiftNotificationRepository.findAllByProcessedIsFalse()).thenReturn(shiftNotifications)
      whenever(userPreferenceService.getUserPreference(quantumId)).thenReturn(UserPreference(quantumId, null, "email", "sms", CommunicationPreference.EMAIL))
      whenever(notifyClient.sendEmail(any(), eq("email"), any(), isNull())).thenThrow(NotificationClientException(quantumId))

      service.sendNotifications()

      verify(userPreferenceService).getUserPreference(quantumId)
      verify(notifyClient).sendEmail(any(), eq("email"), any(), isNull())
      verify(shiftNotificationRepository, times(0)).saveAll(shiftNotifications)
    }
  }
}
