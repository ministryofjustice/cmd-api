package uk.gov.justice.digital.hmpps.cmd.api.service

import com.microsoft.applicationinsights.TelemetryClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.reset
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
internal class NotificationServiceAllowlistTest {
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
    allowedUsersToNotify = setOf("XYZ"),
  )

  private val now = LocalDateTime.now(clock)

  @BeforeEach
  fun resetAllMocks() {
    reset(shiftNotificationRepository, userPreferenceService, notifyClient)
  }

  @Nested
  @DisplayName("Allowlist sned notification tests")
  inner class SendNotificationWithSnoozeTests {
    @Test
    fun `Should allow notifications to be sent for XYZ`() {
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
    fun `Should disallow notifications to be sent for ABC`() {
      val quantumId1 = "ABC"
      val shiftNotifications: List<Notification> = listOf(
        Notification(1, quantumId1, now.plusDays(4), now.plusDays(4), now.plusDays(4), null, ShiftType.SHIFT, DetailModificationType.ADD, false),
      )

      whenever(shiftNotificationRepository.findAllByProcessedIsFalse()).thenReturn(shiftNotifications)
      whenever(userPreferenceService.getUserPreference(quantumId1)).thenReturn(UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL))
      whenever(notifyClient.sendEmail(any(), eq("email"), any(), any())).thenReturn(null)
      whenever(shiftNotificationRepository.saveAll(shiftNotifications)).thenReturn(shiftNotifications)

      service.sendNotifications()

      verify(shiftNotificationRepository).findAllByProcessedIsFalse()
      verify(userPreferenceService, times(0)).getUserPreference(quantumId1)
      verify(notifyClient, times(0)).sendEmail(any(), eq("email"), any(), any())
      verify(shiftNotificationRepository).saveAll(shiftNotifications)
    }
  }
}
