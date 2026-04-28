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
import org.mockito.kotlin.isNull
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
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
import java.time.ZoneId

@DisplayName("Notification Service tests")
internal class NotificationServiceTestSendDuplicates {
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

  @BeforeEach
  fun resetAllMocks() {
    reset(shiftNotificationRepository, userPreferenceService, notifyClient)
  }

  @Nested
  @DisplayName("Send Notification tests - duplicate")
  inner class SendNotificationTests {

    @Test
    fun `Should only send most recent notification for duplicates`() {
      val frozenClock = Clock.fixed(LocalDate.of(2010, 10, 29).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
      val quantumId = "CSTRIFE_GEN"
      val date = LocalDateTime.now(frozenClock).plusDays(4)
      val modified1 = LocalDateTime.now(frozenClock).plusHours(1)
      val modified2 = LocalDateTime.now(frozenClock).plusHours(2)
      val modified3 = LocalDateTime.now(frozenClock).plusHours(3)

      val shiftNotifications: List<Notification> = listOf(
        Notification(1, quantumId, modified1, date, date, null, ShiftType.SHIFT, DetailModificationType.ADD, false),
        Notification(1, quantumId, modified2, date, date, null, ShiftType.SHIFT, DetailModificationType.ADD, false),
        Notification(1, quantumId, modified3, date, date, null, ShiftType.SHIFT, DetailModificationType.ADD, false),
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

      assertThat(slot.firstValue.getValue("not1")).isEqualTo("* Your shift on Tuesday, 2nd November has been added.")
      assertThat(slot.firstValue.getValue("not2")).isEqualTo("")
    }

    @Test
    fun `Should only send most recent notification for duplicates if some are tasks too`() {
      val frozenClock = Clock.fixed(LocalDate.of(2010, 10, 29).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
      val quantumId = "CSTRIFE_GEN"
      val date = LocalDate.now(frozenClock).plusDays(4)
      val modified1 = LocalDateTime.now(frozenClock).plusHours(1)

      val shiftNotifications: List<Notification> = listOf(
        Notification(1, quantumId, modified1, date.atStartOfDay().plusSeconds(0), date.atStartOfDay().plusSeconds(0), null, ShiftType.SHIFT, DetailModificationType.ADD, false),
        Notification(1, quantumId, modified1, date.atStartOfDay().plusSeconds(0), date.atStartOfDay().plusSeconds(0), null, ShiftType.SHIFT, DetailModificationType.ADD, false),
        Notification(1, quantumId, modified1, date.atStartOfDay().plusSeconds(9876), date.atStartOfDay().plusSeconds(6544), "A Task", ShiftType.SHIFT, DetailModificationType.ADD, false),
        Notification(1, quantumId, modified1, date.atStartOfDay().plusSeconds(1234), date.atStartOfDay().plusSeconds(4567), "Any Task", ShiftType.SHIFT, DetailModificationType.ADD, false),
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

      assertThat(slot.firstValue.getValue("not1")).isEqualTo("* Your shift on Tuesday, 2nd November has been added.")
      assertThat(slot.firstValue.getValue("not2")).isEqualTo("* Your detail on Tuesday, 2nd November (00:20:34 - 01:16:07) has been added.")
      assertThat(slot.firstValue.getValue("not3")).isEqualTo("* Your detail on Tuesday, 2nd November (02:44:36 - 01:49:04) has been added.")
    }
  }
}
