package uk.gov.justice.digital.hmpps.cmd.api.service

import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.cmd.api.model.Notification
import uk.gov.justice.digital.hmpps.cmd.api.model.UserPreference
import uk.gov.justice.digital.hmpps.cmd.api.repository.NotificationRepository
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrClient
import uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailModificationType
import uk.gov.justice.digital.hmpps.cmd.api.service.PrisonService
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.DetailParentType
import uk.gov.service.notify.NotificationClient
import java.time.*

@ExtendWith(MockKExtension::class)
@DisplayName("Notification Service tests")
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
            csrClient
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
        fun `Should combine notifications to one user`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<Notification> = listOf(
                    Notification(1, quantumId1, now.plusDays(4), now.plusDays(4), now.plusDays(4), null, DetailParentType.SHIFT, DetailModificationType.ADD, false),
                    Notification(2, quantumId1, now.plusDays(5), now.plusDays(4), now.plusDays(4), null, DetailParentType.SHIFT, DetailModificationType.ADD, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL)
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
            val shiftNotifications: List<Notification> = listOf(
                    Notification(1, quantumId1, now.plusDays(4), now.plusDays(4),now.plusDays(4), null, DetailParentType.SHIFT, DetailModificationType.ADD, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null
            every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
            verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), null) }
            verify { shiftNotificationRepository.saveAll(shiftNotifications) }
        }

        @Test
        fun `Should send notifications to two users`() {
            val quantumId1 = "XYZ"
            val quantumId2 = "ABC"
            val shiftNotifications: List<Notification> = listOf(
                    Notification(1, quantumId1, now.plusDays(4), now.plusDays(4), now.plusDays(4), null, DetailParentType.SHIFT, DetailModificationType.ADD, false),
                    Notification(2, quantumId2, now.plusDays(5), now.plusDays(5), now.plusDays(5), null, DetailParentType.SHIFT, DetailModificationType.ADD, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL)
            every { userPreferenceService.getOrCreateUserPreference(quantumId2) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL)
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
            val shiftNotifications: List<Notification> = listOf(
                    Notification(1, quantumId1, now.plusDays(4), now.plusDays(4), now.plusDays(4), null, DetailParentType.SHIFT, DetailModificationType.ADD, false),
                    Notification(2, quantumId2, now.plusDays(5), now.plusDays(5), now.plusDays(5), null, DetailParentType.SHIFT, DetailModificationType.ADD, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL)
            every { userPreferenceService.getOrCreateUserPreference(quantumId2) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.SMS)
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

            val shiftNotifications: List<Notification> = listOf(
                    Notification(1, quantumId1, now.plusDays(4), now.plusDays(4), now.plusDays(4), null, DetailParentType.SHIFT, DetailModificationType.ADD, false),
                    Notification(2, quantumId2, now.plusDays(5), now.plusDays(5), now.plusDays(5), null, DetailParentType.SHIFT, DetailModificationType.ADD, false),
                    Notification(3, quantumId3, now.plusDays(5), now.plusDays(5), now.plusDays(5), null, DetailParentType.SHIFT, DetailModificationType.ADD, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL)
            every { userPreferenceService.getOrCreateUserPreference(quantumId2) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.SMS)
            every { userPreferenceService.getOrCreateUserPreference(quantumId3) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.NONE)
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
        fun `Should order events by shiftDate then StartTime`() {
            val frozenClock = Clock.fixed(LocalDate.of(2010, 10, 29).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
            val quantumId = "CSTRIFE_GEN"
            val date = LocalDate.now(frozenClock).plusDays(4).atStartOfDay()
            val modified1 = date.plusHours(1)

            val shiftNotifications: List<Notification> = listOf(
                    Notification(1, quantumId, modified1, date.plusSeconds(0), date.plusSeconds(0), null, DetailParentType.SHIFT, DetailModificationType.ADD, false),
                    Notification(1, quantumId, modified1, date.plusSeconds(1234), date.plusSeconds(4567), "A Task", DetailParentType.SHIFT, DetailModificationType.ADD, false),
                    Notification(1, quantumId, modified1, date.plusSeconds(9876), date.plusSeconds(6544), "Any Task", DetailParentType.SHIFT, DetailModificationType.ADD, false),
                    Notification(1, quantumId, modified1, date.plusSeconds(1234).minusDays(1), date.plusSeconds(4567), "Any Task", DetailParentType.SHIFT, DetailModificationType.ADD, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId) } returns UserPreference(quantumId, null, "email", "sms", CommunicationPreference.EMAIL)
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

            assertThat(slot.captured.getValue("not1")).isEqualTo("* Your detail on Monday, 1st November (00:20:34 - 01:16:07) has been added.")
            assertThat(slot.captured.getValue("not2")).isEqualTo("* Your shift on Tuesday, 2nd November has been added.")
            assertThat(slot.captured.getValue("not3")).isEqualTo("* Your detail on Tuesday, 2nd November (00:20:34 - 01:16:07) has been added.")
            assertThat(slot.captured.getValue("not4")).isEqualTo("* Your detail on Tuesday, 2nd November (02:44:36 - 01:49:04) has been added.")

        }

    }
}