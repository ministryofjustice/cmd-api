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
import uk.gov.justice.digital.hmpps.cmd.api.model.ShiftTaskNotification
import uk.gov.justice.digital.hmpps.cmd.api.repository.ShiftNotificationRepository
import uk.gov.justice.digital.hmpps.cmd.api.repository.ShiftTaskNotificationRepository
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import java.time.*
import java.util.*

@ExtendWith(MockKExtension::class)
@DisplayName("Notification Service tests")
internal class NotificationServiceTest {
    private val shiftNotificationRepository: ShiftNotificationRepository = mockk(relaxUnitFun = true)
    private val shiftTaskNotificationRepository: ShiftTaskNotificationRepository = mockk(relaxUnitFun = true)
    private val authenticationFacade: AuthenticationFacade = mockk(relaxUnitFun = true)
    private val now = LocalDate.now()
    private val clock = Clock.fixed(now.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
    private val service = NotificationService(shiftNotificationRepository, shiftTaskNotificationRepository, clock, authenticationFacade, 3)

    @BeforeEach
    fun resetAllMocks() {
        clearMocks(shiftNotificationRepository)
        clearMocks(shiftTaskNotificationRepository)
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
            val shiftTaskNotifications = listOf(getValidShiftTaskNotification())
            every { shiftNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
            every { shiftTaskNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftTaskNotifications
            every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications
            every { shiftTaskNotificationRepository.saveAll(shiftTaskNotifications) } returns shiftTaskNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getNotifications(unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
            verify { shiftNotificationRepository.saveAll(shiftNotifications) }
            confirmVerified(shiftNotificationRepository)
            verify { shiftTaskNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
            verify { shiftTaskNotificationRepository.saveAll(shiftTaskNotifications) }
            confirmVerified(shiftTaskNotificationRepository)

            assertThat(returnValue).hasSize(2)
        }

        @Test
        fun `Should get Notifications when there is only a shift notification`() {
            val quantumId = "XYZ"
            val from = Optional.of(LocalDate.now(clock).minusDays(1))
            val to = Optional.of(LocalDate.now(clock).plusDays(1))
            val unprocessedOnly = Optional.of(false)

            val shiftNotifications = listOf(getValidShiftNotification())
            val shiftTaskNotifications: List<ShiftTaskNotification> = listOf()
            every { shiftNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
            every { shiftTaskNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftTaskNotifications
            every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications
            every { shiftTaskNotificationRepository.saveAll(shiftTaskNotifications) } returns shiftTaskNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getNotifications(unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
            verify { shiftNotificationRepository.saveAll(shiftNotifications) }
            confirmVerified(shiftNotificationRepository)
            verify { shiftTaskNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
            verify { shiftTaskNotificationRepository.saveAll(shiftTaskNotifications) }
            confirmVerified(shiftTaskNotificationRepository)

            assertThat(returnValue).hasSize(1)
        }

        @Test
        fun `Should get Notifications when there is only a shift task notification`() {
            val quantumId = "XYZ"
            val from = Optional.of(LocalDate.now(clock).minusDays(1))
            val to = Optional.of(LocalDate.now(clock).plusDays(1))
            val unprocessedOnly = Optional.of(false)

            val shiftNotifications: List<ShiftNotification> = listOf()
            val shiftTaskNotifications = listOf(getValidShiftTaskNotification())
            every { shiftNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
            every { shiftTaskNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftTaskNotifications
            every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications
            every { shiftTaskNotificationRepository.saveAll(shiftTaskNotifications) } returns shiftTaskNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getNotifications(unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
            verify { shiftNotificationRepository.saveAll(shiftNotifications) }
            confirmVerified(shiftNotificationRepository)
            verify { shiftTaskNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
            verify { shiftTaskNotificationRepository.saveAll(shiftTaskNotifications) }
            confirmVerified(shiftTaskNotificationRepository)

            assertThat(returnValue).hasSize(1)
        }

        @Test
        fun `Should not get Notifications when there no notifications`() {
            val quantumId = "XYZ"
            val from = Optional.of(LocalDate.now(clock).minusDays(1))
            val to = Optional.of(LocalDate.now(clock).plusDays(1))
            val unprocessedOnly = Optional.of(false)

            val shiftNotifications: List<ShiftNotification> = listOf()
            val shiftTaskNotifications: List<ShiftTaskNotification> = listOf()
            every { shiftNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
            every { shiftTaskNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftTaskNotifications
            every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications
            every { shiftTaskNotificationRepository.saveAll(shiftTaskNotifications) } returns shiftTaskNotifications
            every { authenticationFacade.currentUsername } returns quantumId
            val returnValue = service.getNotifications(unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
            verify { shiftNotificationRepository.saveAll(shiftNotifications) }
            confirmVerified(shiftNotificationRepository)
            verify { shiftTaskNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
            verify { shiftTaskNotificationRepository.saveAll(shiftTaskNotifications) }
            confirmVerified(shiftTaskNotificationRepository)

            assertThat(returnValue).hasSize(0)
        }

        @Test
        fun `Should use 'from' and 'to' params`() {
            val quantumId = "XYZ"
            val from = Optional.of(LocalDate.now(clock).minusDays(1))
            val to = Optional.of(LocalDate.now(clock).plusDays(1))
            val unprocessedOnly = Optional.of(false)

            val shiftNotifications: List<ShiftNotification> = listOf()
            val shiftTaskNotifications: List<ShiftTaskNotification> = listOf()
            every { shiftNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
            every { shiftTaskNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftTaskNotifications
            every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications
            every { shiftTaskNotificationRepository.saveAll(shiftTaskNotifications) } returns shiftTaskNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            service.getNotifications(unprocessedOnly, from, to)

            // Should use the from and to passed in.
            verify { shiftNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
            verify { shiftNotificationRepository.saveAll(shiftNotifications) }
            confirmVerified(shiftNotificationRepository)
            verify { shiftTaskNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
            verify { shiftTaskNotificationRepository.saveAll(shiftTaskNotifications) }
            confirmVerified(shiftTaskNotificationRepository)

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
            val shiftTaskNotifications: List<ShiftTaskNotification> = listOf()
            every { shiftNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX)) } returns shiftNotifications
            every { shiftTaskNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX)) } returns shiftTaskNotifications
            every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications
            every { shiftTaskNotificationRepository.saveAll(shiftTaskNotifications) } returns shiftTaskNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            service.getNotifications(unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX)) }
            verify { shiftNotificationRepository.saveAll(shiftNotifications) }
            confirmVerified(shiftNotificationRepository)
            verify { shiftTaskNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX)) }
            verify { shiftTaskNotificationRepository.saveAll(shiftTaskNotifications) }
            confirmVerified(shiftTaskNotificationRepository)
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
            val shiftTaskNotifications: List<ShiftTaskNotification> = listOf()
            every { shiftNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
            every { shiftTaskNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftTaskNotifications
            every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications
            every { shiftTaskNotificationRepository.saveAll(shiftTaskNotifications) } returns shiftTaskNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            service.getNotifications(unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
            verify { shiftNotificationRepository.saveAll(shiftNotifications) }
            confirmVerified(shiftNotificationRepository)
            verify { shiftTaskNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
            verify { shiftTaskNotificationRepository.saveAll(shiftTaskNotifications) }
            confirmVerified(shiftTaskNotificationRepository)
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
            val shiftTaskNotifications: List<ShiftTaskNotification> = listOf()
            every { shiftNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX)) } returns shiftNotifications
            every { shiftTaskNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX)) } returns shiftTaskNotifications
            every { shiftNotificationRepository.saveAll(shiftNotifications) } returns shiftNotifications
            every { shiftTaskNotificationRepository.saveAll(shiftTaskNotifications) } returns shiftTaskNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            service.getNotifications(unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX)) }
            verify { shiftNotificationRepository.saveAll(shiftNotifications) }
            confirmVerified(shiftNotificationRepository)
            verify { shiftTaskNotificationRepository.findAllByQuantumIdAndLastModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX)) }
            verify { shiftTaskNotificationRepository.saveAll(shiftTaskNotifications) }
            confirmVerified(shiftTaskNotificationRepository)
        }
    }

    companion object {
        fun getValidShiftNotification(): ShiftNotification {
            val date = LocalDateTime.now()

            val quantumId = "XYZ"
            val description = "Any Description,"
            val shiftDate = date.plusDays(2)
            val lastModified = date.plusDays(3)
            val notificationType = 0L
            val processed = false

            return ShiftNotification(
                    quantumId,
                    description,
                    shiftDate,
                    lastModified,
                    notificationType,
                    processed
            )
        }

        fun getValidShiftTaskNotification(): ShiftTaskNotification {
            val date = LocalDateTime.now()

            val quantumId = "XYZ"
            val description = "Any Description,"
            val taskDate = date.plusDays(2)
            val taskStartSec = 123
            val taskEndSec = 456
            val activity = "Any Activity"
            val lastModified = date.plusDays(3)
            val processed = false

            return ShiftTaskNotification(
                    quantumId,
                    description,
                    taskDate,
                    taskStartSec,
                    taskEndSec,
                    activity,
                    lastModified,
                    processed
            )
        }
    }
}