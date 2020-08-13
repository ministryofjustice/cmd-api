package uk.gov.justice.digital.hmpps.cmd.api.service

import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.cmd.api.client.PrisonDiaryClient
import uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference
import uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftNotificaitonActionType
import uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftNotificationType
import uk.gov.justice.digital.hmpps.cmd.api.model.ShiftNotification
import uk.gov.justice.digital.hmpps.cmd.api.model.UserPreference
import uk.gov.justice.digital.hmpps.cmd.api.repository.ShiftNotificationRepository
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
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
    private val prisonDiaryClient: PrisonDiaryClient = mockk(relaxUnitFun = true)
    private val clock = Clock.fixed(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
    private val service = NotificationService(
            shiftNotificationRepository,
            userPreferenceService,
            clock,
            authenticationFacade,
            3,
            notifyClient,
            prisonService,
            prisonDiaryClient
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
            every { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getNotifications(processOnRead, unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
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
            every { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getNotifications(processOnRead, unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
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
            every { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId
            val returnValue = service.getNotifications(processOnRead, unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
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
            every { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            service.getNotifications(processOnRead, unprocessedOnly, from, to)

            // Should use the from and to passed in.
            verify { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
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
            every { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX)) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            service.getNotifications(processOnRead, unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX)) }
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
            every { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            service.getNotifications(processOnRead, unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
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
            every { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX)) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            service.getNotifications(processOnRead, unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX)) }
            confirmVerified(shiftNotificationRepository)
        }
    }

    @Nested
    @DisplayName("Send Notification tests")
    inner class SendNotificationTests {

        @BeforeEach
        // We don't care about this first part for these tests
        fun `set up notification fetching`(){
            every { prisonService.getAllPrisons() } returns listOf()
            every { shiftNotificationRepository.saveAll<ShiftNotification>(any()) } returns listOf()
        }

        @AfterEach
        fun `verify nothing else happsns`() {
            verify(exactly = 1) { shiftNotificationRepository.saveAll<ShiftNotification>(any()) }
            confirmVerified(shiftNotificationRepository)
            confirmVerified(userPreferenceService)
            confirmVerified(notifyClient)
        }

        @Test
        fun `Should do nothing if there are no notifications`() {
            val shiftNotifications: List<ShiftNotification> = listOf()

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications

            service.refreshNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
        }

        @Test
        fun `Should send a notification to one user`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDateTime.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftNotificaitonActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null

            service.refreshNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
            verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), null) }
        }

        @Test
        fun `Should not send a notification to one user if they have a blank Email and Email Preference`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDateTime.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftNotificaitonActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "", "sms", CommunicationPreference.EMAIL.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null

            service.refreshNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
        }

        @Test
        fun `Should not send a notification to one user if they have a null Email and Email Preference`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDateTime.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftNotificaitonActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, null, "sms", CommunicationPreference.EMAIL.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null

            service.refreshNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
        }

        @Test
        fun `Should not send a notification to one user if they have a blank Sms and Sms Preference`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDateTime.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftNotificaitonActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "", CommunicationPreference.SMS.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null

            service.refreshNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
        }

        @Test
        fun `Should not send a notification to one user if they have a null Sms and Sms Preference`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDateTime.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftNotificaitonActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", null, CommunicationPreference.SMS.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null

            service.refreshNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
        }

        @Test
        fun `Should combine notifications to one user`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDateTime.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftNotificaitonActionType.ADD.value, false),
                    ShiftNotification(2, quantumId1, LocalDateTime.now(clock).plusDays(5), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftNotificaitonActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null

            service.refreshNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
            verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), any()) }
        }

        @Test
        fun `Should respect communication preferences Email`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDateTime.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftNotificaitonActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null

            service.refreshNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
            verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), any()) }
        }

        @Test
        fun `Should respect communication preferences Sms`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDateTime.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftNotificaitonActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.SMS.value)
            every { notifyClient.sendSms(any(), "sms", any(), any()) } returns null

            service.refreshNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
            verify(exactly = 1) { notifyClient.sendSms(any(), "sms", any(), null) }
        }

        @Test
        fun `Should respect communication preferences None`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDateTime.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftNotificaitonActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.NONE.value)

            service.refreshNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
        }

        @Test
        fun `Should send notifications to two users`() {
            val quantumId1 = "XYZ"
            val quantumId2 = "ABC"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDateTime.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftNotificaitonActionType.ADD.value, false),
                    ShiftNotification(2, quantumId2, LocalDateTime.now(clock).plusDays(5), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftNotificaitonActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL.value)
            every { userPreferenceService.getOrCreateUserPreference(quantumId2) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null

            service.refreshNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId2) }
            verify(exactly = 2) { notifyClient.sendEmail(any(), "email", any(), null) }
        }

        @Test
        fun `Should send notifications to two users with different preferences`() {
            val quantumId1 = "XYZ"
            val quantumId2 = "ABC"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDateTime.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftNotificaitonActionType.ADD.value, false),
                    ShiftNotification(2, quantumId2, LocalDateTime.now(clock).plusDays(5), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftNotificaitonActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL.value)
            every { userPreferenceService.getOrCreateUserPreference(quantumId2) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.SMS.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null
            every { notifyClient.sendSms(any(), "sms", any(), any()) } returns null

            service.refreshNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId2) }
            verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), null) }
            verify(exactly = 1) { notifyClient.sendSms(any(), "sms", any(), null) }
        }

        @Test
        fun `Should send notifications to two users with different preferences when the third one is 'NONE'`() {
            val quantumId1 = "XYZ"
            val quantumId2 = "ABC"
            val quantumId3 = "123"

            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDateTime.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftNotificaitonActionType.ADD.value, false),
                    ShiftNotification(2, quantumId2, LocalDateTime.now(clock).plusDays(5), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftNotificaitonActionType.ADD.value, false),
                    ShiftNotification(3, quantumId3, LocalDateTime.now(clock).plusDays(5), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftNotificaitonActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL.value)
            every { userPreferenceService.getOrCreateUserPreference(quantumId2) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.SMS.value)
            every { userPreferenceService.getOrCreateUserPreference(quantumId3) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.NONE.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null
            every { notifyClient.sendSms(any(), "sms", any(), any()) } returns null


            service.refreshNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId2) }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId3) }
            verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), null) }
            verify(exactly = 1) { notifyClient.sendSms(any(), "sms", any(), null) }
        }

    }

    @Nested
    @DisplayName("Snooze data specific notify tests")
    inner class SendNotificationWithSnoozeTests {

        @BeforeEach
        // We don't care about this first part for these tests
        fun `set up notification fetching`(){
            every { prisonService.getAllPrisons() } returns listOf()
            every { shiftNotificationRepository.saveAll<ShiftNotification>(any()) } returns listOf()
        }

        @AfterEach
        fun `verify nothing else happens`() {
            verify(exactly = 1) { shiftNotificationRepository.saveAll<ShiftNotification>(any()) }
            confirmVerified(shiftNotificationRepository)
            confirmVerified(userPreferenceService)
            confirmVerified(notifyClient)
        }

        @Test
        fun `Should not send a notification if the user has a snooze preference set to future date`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDateTime.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftNotificaitonActionType.ADD.value, false)
            )

            val snoozePref = LocalDate.now(clock).plusDays(20)
            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, snoozePref, "email", "sms", CommunicationPreference.EMAIL.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null

            service.refreshNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
        }

        @Test
        fun `Should not send a notification if the user has a snooze preference set to today's date`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDateTime.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftNotificaitonActionType.ADD.value, false)
            )

            val snoozePref = LocalDate.now(clock)
            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, snoozePref, "email", "sms", CommunicationPreference.EMAIL.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null

            service.refreshNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }

        }

        @Test
        fun `Should send a notification if the user has a snooze preference set to yesterday's date`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDateTime.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftNotificaitonActionType.ADD.value, false)
            )

            val snoozePref = LocalDate.now(clock).minusDays(1)
            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, snoozePref, "email", "sms", CommunicationPreference.EMAIL.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null

            service.refreshNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
            verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), null) }

        }
    }


    companion object {
        fun getValidShiftNotification(clock: Clock): ShiftNotification {
            val date = LocalDateTime.now(clock)

            val quantumId = "XYZ"
            val shiftDate = date.plusDays(2)
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