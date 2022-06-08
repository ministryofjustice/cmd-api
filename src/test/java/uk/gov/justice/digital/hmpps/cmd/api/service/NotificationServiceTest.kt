package uk.gov.justice.digital.hmpps.cmd.api.service

import com.microsoft.applicationinsights.TelemetryClient
import io.mockk.clearMocks
import io.mockk.confirmVerified
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
import uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailModificationType
import uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.cmd.api.model.Notification
import uk.gov.justice.digital.hmpps.cmd.api.model.UserPreference
import uk.gov.justice.digital.hmpps.cmd.api.repository.NotificationRepository
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import uk.gov.service.notify.NotificationClient
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Optional

@ExtendWith(MockKExtension::class)
@DisplayName("Notification Service tests")
internal class NotificationServiceTest {
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
  private val from = Optional.of(LocalDate.now(clock).minusDays(1))
  private val to = Optional.of(LocalDate.now(clock).plusDays(1))

  @BeforeEach
  fun resetAllMocks() {
    clearMocks(shiftNotificationRepository)
    clearMocks(userPreferenceService)
    clearMocks(notifyClient)
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
      every { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
      every { authenticationFacade.currentUsername } returns quantumId
      every { shiftNotificationRepository.saveAll(shiftNotifications) } returns listOf()
      val returnValue = service.getNotifications(processOnRead, unprocessedOnly, from, to)

      verify { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
      verify { shiftNotificationRepository.saveAll(shiftNotifications) }
      confirmVerified(shiftNotificationRepository)

      assertThat(returnValue).hasSize(1)
    }

    @Test
    fun `Should filter duplicate notifications Notifications`() {
      val quantumId = "XYZ"
      val unprocessedOnly = Optional.of(false)
      val processOnRead = Optional.of(true)

      val shiftNotifications = listOf(getValidNotification(now), getValidNotification(now), getValidNotification(now))
      every { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
      every { authenticationFacade.currentUsername } returns quantumId
      every { shiftNotificationRepository.saveAll(shiftNotifications) } returns listOf()

      val returnValue = service.getNotifications(processOnRead, unprocessedOnly, from, to)

      verify { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
      verify { shiftNotificationRepository.saveAll(shiftNotifications) }
      confirmVerified(shiftNotificationRepository)

      assertThat(returnValue).hasSize(1)
    }

    @Test
    fun `Should get Notifications when there is only a shift notification`() {
      val quantumId = "XYZ"

      val unprocessedOnly = Optional.of(false)
      val processOnRead = Optional.of(true)

      val shiftNotifications = listOf(getValidNotification(now))
      every { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
      every { authenticationFacade.currentUsername } returns quantumId
      every { shiftNotificationRepository.saveAll(shiftNotifications) } returns listOf()

      val returnValue = service.getNotifications(processOnRead, unprocessedOnly, from, to)

      verify { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
      verify { shiftNotificationRepository.saveAll(shiftNotifications) }
      confirmVerified(shiftNotificationRepository)

      assertThat(returnValue).hasSize(1)
    }

    @Test
    fun `Should not get Notifications when there no notifications`() {
      val quantumId = "XYZ"

      val unprocessedOnly = Optional.of(false)
      val processOnRead = Optional.of(true)

      val shiftNotifications: List<Notification> = listOf()
      every { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
      every { authenticationFacade.currentUsername } returns quantumId
      every { shiftNotificationRepository.saveAll(shiftNotifications) } returns listOf()

      val returnValue = service.getNotifications(processOnRead, unprocessedOnly, from, to)

      verify { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
      verify { shiftNotificationRepository.saveAll(shiftNotifications) }

      confirmVerified(shiftNotificationRepository)

      assertThat(returnValue).hasSize(0)
    }

    @Test
    fun `Should use 'from' and 'to' params`() {
      val quantumId = "XYZ"

      val unprocessedOnly = Optional.of(false)
      val processOnRead = Optional.of(true)

      val shiftNotifications: List<Notification> = listOf()
      every { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
      every { authenticationFacade.currentUsername } returns quantumId
      every { shiftNotificationRepository.saveAll(shiftNotifications) } returns listOf()

      service.getNotifications(processOnRead, unprocessedOnly, from, to)

      // Should use the from and to passed in.
      verify { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
      verify { shiftNotificationRepository.saveAll(shiftNotifications) }
      confirmVerified(shiftNotificationRepository)
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
      every { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX)) } returns shiftNotifications
      every { authenticationFacade.currentUsername } returns quantumId
      every { shiftNotificationRepository.saveAll(shiftNotifications) } returns listOf()

      service.getNotifications(processOnRead, unprocessedOnly, from, to)

      verify { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX)) }
      verify { shiftNotificationRepository.saveAll(shiftNotifications) }

      confirmVerified(shiftNotificationRepository)
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
      every { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
      every { authenticationFacade.currentUsername } returns quantumId
      every { shiftNotificationRepository.saveAll(shiftNotifications) } returns listOf()

      service.getNotifications(processOnRead, unprocessedOnly, from, to)

      verify { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
      verify { shiftNotificationRepository.saveAll(shiftNotifications) }

      confirmVerified(shiftNotificationRepository)
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
      every { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX)) } returns shiftNotifications
      every { authenticationFacade.currentUsername } returns quantumId
      every { shiftNotificationRepository.saveAll(shiftNotifications) } returns listOf()

      service.getNotifications(processOnRead, unprocessedOnly, from, to)

      verify { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX)) }
      verify { shiftNotificationRepository.saveAll(shiftNotifications) }

      confirmVerified(shiftNotificationRepository)
    }
  }

  @Nested
  @DisplayName("Snooze data specific notify tests")
  inner class SendNotificationWithSnoozeTests {

    @Test
    fun `Should not send a notification if the user has a snooze preference set to future date`() {
      val quantumId1 = "XYZ"
      val shiftNotifications: List<Notification> = listOf(
        Notification(1, quantumId1, now, now, now, null, ShiftType.SHIFT, DetailModificationType.ADD, false)
      )

      val snoozePref = LocalDate.now(clock).plusDays(20)
      every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
      every { userPreferenceService.getUserPreference(quantumId1) } returns UserPreference(quantumId1, snoozePref, "email", "sms", CommunicationPreference.EMAIL)
      every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null
      every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications

      service.sendNotifications()

      verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
      verify { userPreferenceService.getUserPreference(quantumId1) }
      verify { shiftNotificationRepository.saveAll(shiftNotifications) }
    }

    @Test
    fun `Should not send a notification if the user has a snooze preference set to today's date`() {
      val quantumId1 = "XYZ"
      val shiftNotifications: List<Notification> = listOf(
        Notification(1, quantumId1, now, now, now, null, ShiftType.SHIFT, DetailModificationType.ADD, false)
      )

      val snoozePref = LocalDate.now(clock)
      every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
      every { userPreferenceService.getUserPreference(quantumId1) } returns UserPreference(quantumId1, snoozePref, "email", "sms", CommunicationPreference.EMAIL)
      every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null
      every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications

      service.sendNotifications()

      verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
      verify { userPreferenceService.getUserPreference(quantumId1) }
      verify { shiftNotificationRepository.saveAll(shiftNotifications) }
    }

    @Test
    fun `Should send a notification if the user has a snooze preference set to yesterday's date`() {
      val quantumId1 = "XYZ"
      val shiftNotifications: List<Notification> = listOf(
        Notification(1, quantumId1, now, now, now, null, ShiftType.SHIFT, DetailModificationType.ADD, false)
      )

      val snoozePref = LocalDate.now(clock).minusDays(1)
      every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
      every { userPreferenceService.getUserPreference(quantumId1) } returns UserPreference(quantumId1, snoozePref, "email", "sms", CommunicationPreference.EMAIL)
      every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null
      every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications

      service.sendNotifications()

      verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
      verify { userPreferenceService.getUserPreference(quantumId1) }
      verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), null) }
      verify { shiftNotificationRepository.saveAll(shiftNotifications) }
    }

    @Test
    fun `Should respect communication preferences Email`() {
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
      verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), any()) }
      verify { shiftNotificationRepository.saveAll(shiftNotifications) }
    }

    @Test
    fun `Should respect communication preferences Sms`() {
      val quantumId1 = "XYZ"
      val shiftNotifications: List<Notification> = listOf(
        Notification(1, quantumId1, now.plusDays(4), now.plusDays(4), now.plusDays(4), null, ShiftType.SHIFT, DetailModificationType.ADD, false)
      )

      every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
      every { userPreferenceService.getUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.SMS)
      every { notifyClient.sendSms(any(), "sms", any(), any()) } returns null
      every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications

      service.sendNotifications()

      verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
      verify { userPreferenceService.getUserPreference(quantumId1) }
      verify(exactly = 1) { notifyClient.sendSms(any(), "sms", any(), null) }
      verify { shiftNotificationRepository.saveAll(shiftNotifications) }
    }

    @Test
    fun `Should respect communication preferences None`() {
      val quantumId1 = "XYZ"
      val shiftNotifications: List<Notification> = listOf(
        Notification(1, quantumId1, now.plusDays(4), now.plusDays(4), now.plusDays(4), null, ShiftType.SHIFT, DetailModificationType.ADD, false)
      )

      every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
      every { userPreferenceService.getUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.NONE)
      every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications

      service.sendNotifications()

      verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
      verify { userPreferenceService.getUserPreference(quantumId1) }
      verify { shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 1L }) }
    }

    @Test
    fun `Tidy notifications`() {

      every { shiftNotificationRepository.deleteAllByShiftModifiedBefore(any()) } returns 4

      service.tidyNotification()

      verify { shiftNotificationRepository.deleteAllByShiftModifiedBefore(any()) }
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
        processed
      )
    }
  }
}
