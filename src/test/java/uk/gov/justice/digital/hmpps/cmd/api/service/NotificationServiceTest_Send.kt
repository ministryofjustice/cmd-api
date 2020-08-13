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
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.client.dto.ShiftNotificationDto
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftActionType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftNotificationType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.service.PrisonService
import uk.gov.service.notify.NotificationClient
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@ExtendWith(MockKExtension::class)
@DisplayName("Notification Service tests")
internal class NotificationServiceTest_Send {
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
    @DisplayName("Send Notification tests")
    inner class SendNotificationTests {

        @Test
        fun `Should do nothing if there are no notifications`() {
            val shiftNotifications: List<ShiftNotification> = listOf()

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
        }


        @Test
        fun `Should combine notifications to one user`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDate.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false),
                    ShiftNotification(2, quantumId1, LocalDate.now(clock).plusDays(5), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null
            every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
            verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), any()) }
            verify { shiftNotificationRepository.saveAll(shiftNotifications) }
        }

        @Test
        fun `Should send a notification to one user`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDate.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null
            every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
            verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), null) }
            verify { shiftNotificationRepository.saveAll(shiftNotifications) }
        }

        @Test
        fun `Should respect communication preferences Email`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDate.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null
            every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
            verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), any()) }
            verify { shiftNotificationRepository.saveAll(shiftNotifications) }
        }

        @Test
        fun `Should respect communication preferences Sms`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDate.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.SMS.value)
            every { notifyClient.sendSms(any(), "sms", any(), any()) } returns null
            every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
            verify(exactly = 1) { notifyClient.sendSms(any(), "sms", any(), null) }
            verify { shiftNotificationRepository.saveAll(shiftNotifications) }
        }

        @Test
        fun `Should respect communication preferences None`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDate.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.NONE.value)
            every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
            verify { shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 1L }) }

        }

        @Test
        fun `Should send notifications to two users`() {
            val quantumId1 = "XYZ"
            val quantumId2 = "ABC"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDate.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false),
                    ShiftNotification(2, quantumId2, LocalDate.now(clock).plusDays(5), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL.value)
            every { userPreferenceService.getOrCreateUserPreference(quantumId2) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null
            every { shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 1L }) } returns shiftNotifications
            every { shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 2L }) } returns shiftNotifications

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId2) }
            verify(exactly = 2) { notifyClient.sendEmail(any(), "email", any(), null) }
            verify { shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 1L }) }
        }

        @Test
        fun `Should send notifications to two users with different preferences`() {
            val quantumId1 = "XYZ"
            val quantumId2 = "ABC"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDate.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false),
                    ShiftNotification(2, quantumId2, LocalDate.now(clock).plusDays(5), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL.value)
            every { userPreferenceService.getOrCreateUserPreference(quantumId2) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.SMS.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null
            every { notifyClient.sendSms(any(), "sms", any(), any()) } returns null
            every { shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 1L }) } returns shiftNotifications
            every { shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 2L }) } returns shiftNotifications

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId2) }
            verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), null) }
            verify(exactly = 1) { notifyClient.sendSms(any(), "sms", any(), null) }
            verify { shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 1L }) }
        }

        @Test
        fun `Should send notifications to two users with different preferences when the third one is 'NONE'`() {
            val quantumId1 = "XYZ"
            val quantumId2 = "ABC"
            val quantumId3 = "123"

            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDate.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false),
                    ShiftNotification(2, quantumId2, LocalDate.now(clock).plusDays(5), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false),
                    ShiftNotification(3, quantumId3, LocalDate.now(clock).plusDays(5), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL.value)
            every { userPreferenceService.getOrCreateUserPreference(quantumId2) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.SMS.value)
            every { userPreferenceService.getOrCreateUserPreference(quantumId3) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.NONE.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null
            every { notifyClient.sendSms(any(), "sms", any(), any()) } returns null
            every { shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 1L }) } returns shiftNotifications
            every { shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 2L }) } returns shiftNotifications
            every { shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 3L }) } returns shiftNotifications

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId2) }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId3) }
            verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), null) }
            verify(exactly = 1) { notifyClient.sendSms(any(), "sms", any(), null) }
            verify { shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 1L }) }
            verify { shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 2L }) }
            verify { shiftNotificationRepository.saveAll(shiftNotifications.filter { it.id == 3L }) }

        }

        @Test
        fun `Should only send most recent notification for duplicates`() {
            val frozenClock = Clock.fixed(LocalDate.of(2010, 10, 29).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
            val quantumId = "CSTRIFE_GEN"
            val shiftDate = LocalDate.now(frozenClock).plusDays(4)
            val modified1 = LocalDateTime.now(frozenClock).plusHours(1)
            val modified2 = LocalDateTime.now(frozenClock).plusHours(2)
            val modified3 = LocalDateTime.now(frozenClock).plusHours(3)

            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId, shiftDate, modified1, null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false),
                    ShiftNotification(1, quantumId, shiftDate, modified2, null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false),
                    ShiftNotification(1, quantumId, shiftDate, modified3, null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId) } returns UserPreference(quantumId, null, "email", "sms", CommunicationPreference.EMAIL.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null
            every { notifyClient.sendSms(any(), "sms", any(), any()) } returns null
            every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications

            val slot = slot<Map<String, String>>()
            every { notifyClient.sendEmail(any(), any(), capture(slot), null) } returns null

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId) }
            verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), null) }
            verify(exactly = 0) { notifyClient.sendSms(any(), "sms", any(), null) }
            verify { shiftNotificationRepository.saveAll(shiftNotifications) }

            assertThat(slot.captured.getValue("not1")).isEqualTo("* Your shift on Tuesday, 2nd November has been added.")
            assertThat(slot.captured.getValue("not2")).isEqualTo("")
        }

        @Test
        fun `Should only send most recent notification for duplicates if some are tasks too`() {
            val frozenClock = Clock.fixed(LocalDate.of(2010, 10, 29).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
            val quantumId = "CSTRIFE_GEN"
            val shiftDate = LocalDate.now(frozenClock).plusDays(4)
            val modified1 = LocalDateTime.now(frozenClock).plusHours(1)

            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId, shiftDate, modified1, null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false),
                    ShiftNotification(1, quantumId, shiftDate, modified1, 9876, 6544, "A Task", ShiftNotificationType.SHIFT_TASK.value, ShiftActionType.ADD.value, false),
                    ShiftNotification(1, quantumId, shiftDate, modified1, 1234, 4567, "Any Task", ShiftNotificationType.SHIFT_TASK.value, ShiftActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId) } returns UserPreference(quantumId, null, "email", "sms", CommunicationPreference.EMAIL.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null
            every { notifyClient.sendSms(any(), "sms", any(), any()) } returns null
            every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications

            val slot = slot<Map<String, String>>()
            every { notifyClient.sendEmail(any(), any(), capture(slot), null) } returns null

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId) }
            verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), null) }
            verify(exactly = 0) { notifyClient.sendSms(any(), "sms", any(), null) }
            verify { shiftNotificationRepository.saveAll(shiftNotifications) }

            assertThat(slot.captured.getValue("not1")).isEqualTo("* Your shift on Tuesday, 2nd November has been added.")
            assertThat(slot.captured.getValue("not2")).isEqualTo("* Your activity on Tuesday, 2nd November (02:44:36 - 01:49:04) has been added.")
            assertThat(slot.captured.getValue("not3")).isEqualTo("* Your activity on Tuesday, 2nd November (00:20:34 - 01:16:07) has been added.")
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