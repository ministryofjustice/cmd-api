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
internal class NotificationServiceTest_Send_Duplicates {
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
                    Notification(1, quantumId, modified1, date, date, null, DetailParentType.SHIFT, DetailModificationType.ADD, false),
                    Notification(1, quantumId, modified2, date, date, null, DetailParentType.SHIFT, DetailModificationType.ADD, false),
                    Notification(1, quantumId, modified3, date, date, null, DetailParentType.SHIFT, DetailModificationType.ADD, false)
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

            assertThat(slot.captured.getValue("not1")).isEqualTo("* Your shift on Tuesday, 2nd November has been added.")
            assertThat(slot.captured.getValue("not2")).isEqualTo("")
        }

        @Test
        fun `Should only send most recent notification for duplicates if some are tasks too`() {
            val frozenClock = Clock.fixed(LocalDate.of(2010, 10, 29).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
            val quantumId = "CSTRIFE_GEN"
            val date = LocalDate.now(frozenClock).plusDays(4)
            val modified1 = LocalDateTime.now(frozenClock).plusHours(1)

            val shiftNotifications: List<Notification> = listOf(
                    Notification(1, quantumId, modified1, date.atStartOfDay().plusSeconds(0), date.atStartOfDay().plusSeconds(0), null, DetailParentType.SHIFT, DetailModificationType.ADD, false),
                    Notification(1, quantumId, modified1, date.atStartOfDay().plusSeconds(0), date.atStartOfDay().plusSeconds(0), null, DetailParentType.SHIFT, DetailModificationType.ADD, false),
                    Notification(1, quantumId, modified1, date.atStartOfDay().plusSeconds(9876), date.atStartOfDay().plusSeconds(6544), "A Task", DetailParentType.SHIFT, DetailModificationType.ADD, false),
                    Notification(1, quantumId, modified1, date.atStartOfDay().plusSeconds(1234), date.atStartOfDay().plusSeconds(4567), "Any Task", DetailParentType.SHIFT, DetailModificationType.ADD, false)
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

            assertThat(slot.captured.getValue("not1")).isEqualTo("* Your shift on Tuesday, 2nd November has been added.")
            assertThat(slot.captured.getValue("not2")).isEqualTo("* Your detail on Tuesday, 2nd November (00:20:34 - 01:16:07) has been added.")
            assertThat(slot.captured.getValue("not3")).isEqualTo("* Your detail on Tuesday, 2nd November (02:44:36 - 01:49:04) has been added.")
        }

    }

    companion object {
        fun getValidNotification(clock: Clock): Notification {
            val date = LocalDate.now(clock)

            val quantumId = "XYZ"
            val shiftModified = date.plusDays(3)
            val taskStart = date.atStartOfDay().plusSeconds(123L)
            val taskEnd = date.atStartOfDay().plusSeconds(456L)
            val task = "Any Activity"
            val shiftType = DetailParentType.SHIFT
            val actionType = DetailModificationType.ADD

            val processed = false

            return Notification(
                    1L,
                    quantumId,
                    shiftModified.atStartOfDay(),
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