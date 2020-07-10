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
    private val authenticationFacade: AuthenticationFacade = mockk(relaxUnitFun = true)
    private val notifyClient: NotificationClient = mockk(relaxUnitFun = true)
    private val now = LocalDate.now()
    private val clock = Clock.fixed(now.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
    private val service = NotificationService(shiftNotificationRepository, userPreferenceService, clock, authenticationFacade, 3, notifyClient)

    @BeforeEach
    fun resetAllMocks() {
        clearMocks(shiftNotificationRepository)
    }

    @Nested
    @DisplayName("Get Notification tests")
    inner class GetPreferenceTests {

        @Test
        fun `Should get Notifications`() {
            val quantumId = "XYZ"
            val from = Optional.of(LocalDate.now(clock).minusDays(1))
            val to = Optional.of(LocalDate.now(clock).plusDays(1))
            val unprocessedOnly = Optional.of(false)

            val shiftNotifications = listOf(getValidShiftNotification())
            every { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
            every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getNotifications(unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
            verify { shiftNotificationRepository.saveAll(shiftNotifications) }
            confirmVerified(shiftNotificationRepository)

            assertThat(returnValue).hasSize(1)
        }

        @Test
        fun `Should get Notifications when there is only a shift notification`() {
            val quantumId = "XYZ"
            val from = Optional.of(LocalDate.now(clock).minusDays(1))
            val to = Optional.of(LocalDate.now(clock).plusDays(1))
            val unprocessedOnly = Optional.of(false)

            val shiftNotifications = listOf(getValidShiftNotification())
            every { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
            every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getNotifications(unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
            verify { shiftNotificationRepository.saveAll(shiftNotifications) }
            confirmVerified(shiftNotificationRepository)

            assertThat(returnValue).hasSize(1)
        }

        @Test
        fun `Should not get Notifications when there no notifications`() {
            val quantumId = "XYZ"
            val from = Optional.of(LocalDate.now(clock).minusDays(1))
            val to = Optional.of(LocalDate.now(clock).plusDays(1))
            val unprocessedOnly = Optional.of(false)

            val shiftNotifications: List<ShiftNotification> = listOf()
            every { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
            every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId
            val returnValue = service.getNotifications(unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
            verify { shiftNotificationRepository.saveAll(shiftNotifications) }
            confirmVerified(shiftNotificationRepository)

            assertThat(returnValue).hasSize(0)
        }

        @Test
        fun `Should use 'from' and 'to' params`() {
            val quantumId = "XYZ"
            val from = Optional.of(LocalDate.now(clock).minusDays(1))
            val to = Optional.of(LocalDate.now(clock).plusDays(1))
            val unprocessedOnly = Optional.of(false)

            val shiftNotifications: List<ShiftNotification> = listOf()
            every { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
            every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            service.getNotifications(unprocessedOnly, from, to)

            // Should use the from and to passed in.
            verify { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
            verify { shiftNotificationRepository.saveAll(shiftNotifications) }
            confirmVerified(shiftNotificationRepository)
        }

        @Test
        fun `Should use defaults if 'from' and 'to' params are empty`() {
            val quantumId = "XYZ"
            val from = Optional.empty<LocalDate>()
            val to = Optional.empty<LocalDate>()
            val unprocessedOnly = Optional.of(false)

            // Should use class defaults.
            val defaultFrom = LocalDate.now(clock).withDayOfMonth(1)
            val toDate = defaultFrom.plusMonths(3)
            val defaultTo = toDate.withDayOfMonth(toDate.lengthOfMonth())

            val shiftNotifications: List<ShiftNotification> = listOf()
            every { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX)) } returns shiftNotifications
            every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            service.getNotifications(unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX)) }
            verify { shiftNotificationRepository.saveAll(shiftNotifications) }
            confirmVerified(shiftNotificationRepository)
        }

        @Test
        fun `Should use default if 'from' param is empty`() {
            val quantumId = "XYZ"
            val from = Optional.empty<LocalDate>()
            val to = Optional.of(LocalDate.now(clock))
            val unprocessedOnly = Optional.of(false)

            // Should count back 3 months to create the 'to'.
            val defaultFrom = to.get().minusMonths(3).withDayOfMonth(1)

            val shiftNotifications: List<ShiftNotification> = listOf()
            every { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
            every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            service.getNotifications(unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
            verify { shiftNotificationRepository.saveAll(shiftNotifications) }
            confirmVerified(shiftNotificationRepository)
        }

        @Test
        fun `Should use default if 'to' param is empty`() {
            val quantumId = "XYZ"
            val from = Optional.of(LocalDate.now(clock))
            val to = Optional.empty<LocalDate>()
            val unprocessedOnly = Optional.of(false)

            // Should use class defaults.
            val toDate = from.get().plusMonths(3)
            val defaultTo = toDate.withDayOfMonth(toDate.lengthOfMonth())

            val shiftNotifications: List<ShiftNotification> = listOf()
            every { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX)) } returns shiftNotifications
            every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            service.getNotifications(unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX)) }
            verify { shiftNotificationRepository.saveAll(shiftNotifications) }
            confirmVerified(shiftNotificationRepository)
        }
    }

    companion object {
        fun getValidShiftNotification(): ShiftNotification {
            val date = LocalDateTime.now()

            val quantumId = "XYZ"
            val shiftDate = date.plusDays(2)
            val shiftModified = date.plusDays(3)
            val taskStart = 123
            val taskEnd = 456
            val task = "Any Activity"
            val shiftType = "SHIFT"
            val actionType = "ADD"

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