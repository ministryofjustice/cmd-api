package uk.gov.justice.digital.hmpps.cmd.api.service

import com.microsoft.applicationinsights.TelemetryClient
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrClient
import uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailModificationType
import uk.gov.justice.digital.hmpps.cmd.api.model.Notification
import uk.gov.justice.digital.hmpps.cmd.api.model.UserPreference
import uk.gov.justice.digital.hmpps.cmd.api.repository.NotificationRepository
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import uk.gov.service.notify.NotificationClient
import uk.gov.service.notify.NotificationClientException
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@ExtendWith(MockKExtension::class)
@DisplayName("Notification Service tests - send")
internal class NotificationServiceTest_Send {
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

  private val now = LocalDateTime.now(clock)

  @BeforeEach
  fun resetAllMocks() {
    clearMocks(shiftNotificationRepository)
    clearMocks(userPreferenceService)
    clearMocks(notifyClient)
  }

  @Nested
  @DisplayName("Send Notification tests")
  inner class SendNotificationTests {

    @Test
    fun `Should do nothing if there are no notifications`() {
      val shiftNotifications: List<Notification> = listOf()

      every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications

      service.sendNotifications()

      verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
    }

    @Test
    fun `Should do nothing if user has no preferences`() {
      val quantumId1 = "XYZ"
      val shiftNotifications: List<Notification> = listOf(
        Notification(1, quantumId1, now, now, now, null, ShiftType.SHIFT, DetailModificationType.ADD, false),
      )
      every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
      every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications
      every { userPreferenceService.getUserPreference(quantumId1) } returns null

      service.sendNotifications()

      verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
      verify { shiftNotificationRepository.saveAll(shiftNotifications) }
      verify { userPreferenceService.getUserPreference(quantumId1) }
      confirmVerified(notifyClient)
    }

    @Test
    fun `Should combine notifications to one user`() {
      val quantumId1 = "XYZ"
      val shiftNotifications: List<Notification> = listOf(
        Notification(1, quantumId1, now.plusDays(4), now.plusDays(4), now.plusDays(4), null, ShiftType.SHIFT, DetailModificationType.ADD, false),
        Notification(2, quantumId1, now.plusDays(5), now.plusDays(4), now.plusDays(4), null, ShiftType.SHIFT, DetailModificationType.ADD, false)
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
    fun `Should send a notification to one user`() {
      val quantumId1 = "XYZ"
      val shiftNotifications: List<Notification> = listOf(
        Notification(1, quantumId1, now.plusDays(4), now.plusDays(4), now.plusDays(4), null, ShiftType.SHIFT, DetailModificationType.ADD, false)
      )

      every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
      every { userPreferenceService.getUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL)
      every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null
      every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications

      service.sendNotifications()

      verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
      verify { userPreferenceService.getUserPreference(quantumId1) }
      verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), null) }
      verify { shiftNotificationRepository.saveAll(shiftNotifications) }
    }

    @Test
    fun `Should send notifications to two users`() {
      val quantumId1 = "XYZ"
      val quantumId2 = "ABC"
      val shiftNotifications: List<Notification> = listOf(
        Notification(1, quantumId1, now.plusDays(4), now.plusDays(4), now.plusDays(4), null, ShiftType.SHIFT, DetailModificationType.ADD, false),
        Notification(2, quantumId2, now.plusDays(5), now.plusDays(5), now.plusDays(5), null, ShiftType.SHIFT, DetailModificationType.ADD, false)
      )

      every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
      every { userPreferenceService.getUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL)
      every { userPreferenceService.getUserPreference(quantumId2) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL)
      every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null
      every { shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 1L }) } returns shiftNotifications
      every { shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 2L }) } returns shiftNotifications

      service.sendNotifications()

      verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
      verify { userPreferenceService.getUserPreference(quantumId1) }
      verify { userPreferenceService.getUserPreference(quantumId2) }
      verify(exactly = 2) { notifyClient.sendEmail(any(), "email", any(), null) }
      verify { shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 1L }) }
    }

    @Test
    fun `Should send notifications to two users with different preferences`() {
      val quantumId1 = "XYZ"
      val quantumId2 = "ABC"
      val shiftNotifications: List<Notification> = listOf(
        Notification(1, quantumId1, now.plusDays(4), now.plusDays(4), now.plusDays(4), null, ShiftType.SHIFT, DetailModificationType.ADD, false),
        Notification(2, quantumId2, now.plusDays(5), now.plusDays(5), now.plusDays(5), null, ShiftType.SHIFT, DetailModificationType.ADD, false)
      )

      every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
      every { userPreferenceService.getUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL)
      every { userPreferenceService.getUserPreference(quantumId2) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.SMS)
      every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null
      every { notifyClient.sendSms(any(), "sms", any(), any()) } returns null
      every { shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 1L }) } returns shiftNotifications
      every { shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 2L }) } returns shiftNotifications

      service.sendNotifications()

      verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
      verify { userPreferenceService.getUserPreference(quantumId1) }
      verify { userPreferenceService.getUserPreference(quantumId2) }
      verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), null) }
      verify(exactly = 1) { notifyClient.sendSms(any(), "sms", any(), null) }
      verify { shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 1L }) }
    }

    @Test
    fun `Should send notifications to two users with different preferences when the third one is 'NONE'`() {
      val quantumId1 = "XYZ"
      val quantumId2 = "ABC"
      val quantumId3 = "123"

      val shiftNotifications: List<Notification> = listOf(
        Notification(1, quantumId1, now.plusDays(4), now.plusDays(4), now.plusDays(4), null, ShiftType.SHIFT, DetailModificationType.ADD, false),
        Notification(2, quantumId2, now.plusDays(5), now.plusDays(5), now.plusDays(5), null, ShiftType.SHIFT, DetailModificationType.ADD, false),
        Notification(3, quantumId3, now.plusDays(5), now.plusDays(5), now.plusDays(5), null, ShiftType.SHIFT, DetailModificationType.ADD, false)
      )

      every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
      every { userPreferenceService.getUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL)
      every { userPreferenceService.getUserPreference(quantumId2) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.SMS)
      every { userPreferenceService.getUserPreference(quantumId3) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.NONE)
      every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null
      every { notifyClient.sendSms(any(), "sms", any(), any()) } returns null
      every { shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 1L }) } returns shiftNotifications
      every { shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 2L }) } returns shiftNotifications
      every { shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 3L }) } returns shiftNotifications

      service.sendNotifications()

      verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
      verify { userPreferenceService.getUserPreference(quantumId1) }
      verify { userPreferenceService.getUserPreference(quantumId2) }
      verify { userPreferenceService.getUserPreference(quantumId3) }
      verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), null) }
      verify(exactly = 1) { notifyClient.sendSms(any(), "sms", any(), null) }
      verify { shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 1L }) }
      verify { shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 2L }) }
      verify { shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 3L }) }
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
        Notification(1, quantumId, modified1, date.plusSeconds(1234).minusDays(1), date.plusSeconds(4567), "Any Task", ShiftType.SHIFT, DetailModificationType.ADD, false)
      )

      every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
      every { userPreferenceService.getUserPreference(quantumId) } returns UserPreference(quantumId, null, "email", "sms", CommunicationPreference.EMAIL)
      every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null
      every { notifyClient.sendSms(any(), "sms", any(), any()) } returns null
      every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications

      val slot = slot<Map<String, String>>()
      every { notifyClient.sendEmail(any(), any(), capture(slot), null) } returns null

      service.sendNotifications()

      verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
      verify { userPreferenceService.getUserPreference(quantumId) }
      verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), null) }
      verify(exactly = 0) { notifyClient.sendSms(any(), "sms", any(), null) }
      verify { shiftNotificationRepository.saveAll(shiftNotifications) }

      assertThat(slot.captured.getValue("not1")).isEqualTo("* Your detail on Monday, 1st November (00:20:34 - 01:16:07) has been added.")
      assertThat(slot.captured.getValue("not2")).isEqualTo("* Your shift on Tuesday, 2nd November has been added.")
      assertThat(slot.captured.getValue("not3")).isEqualTo("* Your detail on Tuesday, 2nd November (00:20:34 - 01:16:07) has been added.")
      assertThat(slot.captured.getValue("not4")).isEqualTo("* Your detail on Tuesday, 2nd November (02:44:36 - 01:49:04) has been added.")
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

      every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
      every { userPreferenceService.getUserPreference(quantumId) } returns UserPreference(quantumId, null, "email", "sms", CommunicationPreference.SMS)
      every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null
      every { notifyClient.sendSms(any(), "sms", any(), any()) } throws NotificationClientException(quantumId)

      service.sendNotifications()

      verify { userPreferenceService.getUserPreference(quantumId) }
      verify { notifyClient.sendSms(any(), "sms", any(), null) }
      verify(exactly = 0) { shiftNotificationRepository.saveAll(shiftNotifications) }
    }
  }
}
