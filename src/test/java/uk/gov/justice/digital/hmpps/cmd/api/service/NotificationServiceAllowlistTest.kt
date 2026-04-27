package uk.gov.justice.digital.hmpps.cmd.api.service

import com.microsoft.applicationinsights.TelemetryClient
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
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
import java.util.Optional

@ExtendWith(MockKExtension::class)
@DisplayName("Notification Service tests")
internal class NotificationServiceAllowlistTest {
  private val shiftNotificationRepository: NotificationRepository = mockk(relaxUnitFun = true)
  private val userPreferenceService: UserPreferenceService = mockk(relaxUnitFun = true)
  private val authenticationFacade: HmppsAuthenticationHolder = mockk(relaxUnitFun = true)
  private val notifyClient: NotificationClient = mockk(relaxUnitFun = true)
  private val csrClient: CsrClient = mockk(relaxUnitFun = true)
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
  private val from = Optional.of(LocalDate.now(clock).minusDays(1))
  private val to = Optional.of(LocalDate.now(clock).plusDays(1))

  @BeforeEach
  fun resetAllMocks() {
    clearMocks(shiftNotificationRepository)
    clearMocks(userPreferenceService)
    clearMocks(notifyClient)
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

      every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
      every { userPreferenceService.getUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL)
      every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null
      every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications

      service.sendNotifications()

      verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
      verify { userPreferenceService.getUserPreference(quantumId1) }
      verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), any()) }
      verify { shiftNotificationRepository.saveAll(shiftNotifications) }
    }

    @Test
    fun `Should disallow notifications to be sent for ABC`() {
      val quantumId1 = "ABC"
      val shiftNotifications: List<Notification> = listOf(
        Notification(1, quantumId1, now.plusDays(4), now.plusDays(4), now.plusDays(4), null, ShiftType.SHIFT, DetailModificationType.ADD, false),
      )

      every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
      every { userPreferenceService.getUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL)
      every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null
      every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications

      service.sendNotifications()

      verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
      verify(exactly = 0) { userPreferenceService.getUserPreference(quantumId1) }
      verify(exactly = 0) { notifyClient.sendEmail(any(), "email", any(), any()) }
      verify { shiftNotificationRepository.saveAll(shiftNotifications) }
    }
  }
}
