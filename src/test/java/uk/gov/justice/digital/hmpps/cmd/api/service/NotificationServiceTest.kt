package uk.gov.justice.digital.hmpps.cmd.api.service

import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.cmd.api.model.ShiftNotification
import uk.gov.justice.digital.hmpps.cmd.api.model.UserPreference
import uk.gov.justice.digital.hmpps.cmd.api.repository.ShiftNotificationRepository
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.client.CsrClient
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftActionType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftNotificationType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.service.PrisonService
import uk.gov.service.notify.NotificationClient
import java.time.*
import java.util.*

@ExtendWith(MockKExtension::class)
@DisplayName("Notification Service tests")
internal class NotificationServiceTest {
    private val shiftNotificationRepository: ShiftNotificationRepository = mockk(relaxUnitFun = true)
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
            csrClient
    )

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
            val from = Optional.of(LocalDate.now(clock).minusDays(1))
            val to = Optional.of(LocalDate.now(clock).plusDays(1))
            val unprocessedOnly = Optional.of(false)
            val processOnRead = Optional.of(true)

            val shiftNotifications = listOf(getValidShiftNotification(clock))
            every { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getNotifications(processOnRead, unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
            confirmVerified(shiftNotificationRepository)

            assertThat(returnValue).hasSize(1)
        }

        @Test
        fun `Should filter duplicate notifications Notifications`() {
            val quantumId = "XYZ"
            val from = Optional.of(LocalDate.now(clock).minusDays(1))
            val to = Optional.of(LocalDate.now(clock).plusDays(1))
            val unprocessedOnly = Optional.of(false)
            val processOnRead = Optional.of(true)

            val shiftNotifications = listOf(getValidShiftNotification(clock),getValidShiftNotification(clock),getValidShiftNotification(clock))
            every { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getNotifications(processOnRead, unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
            confirmVerified(shiftNotificationRepository)

            assertThat(returnValue).hasSize(1)
        }

        @Test
        fun `Should get Notifications when there is only a shift notification`() {
            val quantumId = "XYZ"
            val from = Optional.of(LocalDate.now(clock).minusDays(1))
            val to = Optional.of(LocalDate.now(clock).plusDays(1))
            val unprocessedOnly = Optional.of(false)
            val processOnRead = Optional.of(true)


            val shiftNotifications = listOf(getValidShiftNotification(clock))
            every { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getNotifications(processOnRead, unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
            confirmVerified(shiftNotificationRepository)

            assertThat(returnValue).hasSize(1)
        }

        @Test
        fun `Should not get Notifications when there no notifications`() {
            val quantumId = "XYZ"
            val from = Optional.of(LocalDate.now(clock).minusDays(1))
            val to = Optional.of(LocalDate.now(clock).plusDays(1))
            val unprocessedOnly = Optional.of(false)
            val processOnRead = Optional.of(true)

            val shiftNotifications: List<ShiftNotification> = listOf()
            every { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId
            val returnValue = service.getNotifications(processOnRead, unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
            confirmVerified(shiftNotificationRepository)

            assertThat(returnValue).hasSize(0)
        }

        @Test
        fun `Should use 'from' and 'to' params`() {
            val quantumId = "XYZ"
            val from = Optional.of(LocalDate.now(clock).minusDays(1))
            val to = Optional.of(LocalDate.now(clock).plusDays(1))
            val unprocessedOnly = Optional.of(false)
            val processOnRead = Optional.of(true)

            val shiftNotifications: List<ShiftNotification> = listOf()
            every { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            service.getNotifications(processOnRead, unprocessedOnly, from, to)

            // Should use the from and to passed in.
            verify { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
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

            val shiftNotifications: List<ShiftNotification> = listOf()
            every { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX)) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            service.getNotifications(processOnRead, unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX)) }
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

            val shiftNotifications: List<ShiftNotification> = listOf()
            every { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            service.getNotifications(processOnRead, unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
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

            val shiftNotifications: List<ShiftNotification> = listOf()
            every { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX)) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            service.getNotifications(processOnRead, unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX)) }
            confirmVerified(shiftNotificationRepository)
        }
    }

    @Nested
    @DisplayName("Snooze data specific notify tests")
    inner class SendNotificationWithSnoozeTests {

        @Test
        fun `Should not send a notification if the user has a snooze preference set to future date`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDate.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false)
            )

            val snoozePref = LocalDate.now(clock).plusDays(20)
            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, snoozePref, "email", "sms", CommunicationPreference.EMAIL.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
        }

        @Test
        fun `Should not send a notification if the user has a snooze preference set to today's date`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDate.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false)
            )

            val snoozePref = LocalDate.now(clock)
            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, snoozePref, "email", "sms", CommunicationPreference.EMAIL.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }

        }

        @Test
        fun `Should send a notification if the user has a snooze preference set to yesterday's date`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDate.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false)
            )

            val snoozePref = LocalDate.now(clock).minusDays(1)
            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, snoozePref, "email", "sms", CommunicationPreference.EMAIL.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
            verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), null) }

        }
    }

    companion object {
        fun getValidShiftNotification(clock: Clock): ShiftNotification {
            val date = LocalDateTime.now(clock)

            val quantumId = "XYZ"
            val shiftDate = date.plusDays(2).toLocalDate()
            val shiftModified = date.plusDays(3)
            val taskStart = 123L
            val taskEnd = 456L
            val task = "Any Activity"
            val shiftType = "shift"
            val actionType = "add"

            val processed = false

            return ShiftNotification(
                    1L,
                    quantumId,
                    shiftDate,
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