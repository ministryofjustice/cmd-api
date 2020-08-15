package uk.gov.justice.digital.hmpps.cmd.api.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.cmd.api.model.ShiftNotification
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftActionType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftNotificationType
import java.time.LocalDate
import java.time.LocalDateTime

@ActiveProfiles("test")
@DataJpaTest
class ShiftNotificationRepositoryTest(
        @Autowired val repository: ShiftNotificationRepository
) {

    private val now: LocalDate = LocalDate.now()

    @BeforeEach
    fun resetAllMocks() {
        repository.deleteAll()
    }

    @Nested
    @DisplayName("Get shift notification tests")
    inner class GetShiftNotificationTests {

        @Test
        fun `Should return a notification between the dates case insensitive quantum_id`() {
            val quantumId = "XyZ"
            val date = now.atStartOfDay()
            val notification = getValidShiftNotification(date, date)
            repository.save(notification)

            val notifications = repository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(
                    quantumId,
                    now.minusDays(1).atStartOfDay(),
                    now.plusDays(1).atStartOfDay())
            assertThat(notifications).isNotEmpty

            assertThat(notifications.contains(notification))
        }

        @Test
        fun `Should return a notification between the dates`() {
            val quantumId = "XYZ"
            val date = now.atStartOfDay()
            val notification = getValidShiftNotification(date, date)
            repository.save(notification)

            val notifications = repository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(
                    quantumId,
                    now.minusDays(1).atStartOfDay(),
                    now.plusDays(1).atStartOfDay())
            assertThat(notifications).isNotEmpty

            assertThat(notifications.contains(notification))
        }

        @Test
        fun `Should not return a notification earlier than between the dates`() {
            val quantumId = "XYZ"
            val date = now.minusDays(3).atStartOfDay()
            val notification = getValidShiftNotification(date, date)
            repository.save(notification)

            val notifications = repository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(
                    quantumId,
                    now.minusDays(1).atStartOfDay(),
                    now.plusDays(1).atStartOfDay())
            assertThat(notifications).isEmpty()
        }

        @Test
        fun `Should not return a notification later than between the dates`() {
            val quantumId = "XYZ"
            val date = now.plusDays(3).atStartOfDay()
            val notification = getValidShiftNotification(date, date)
            repository.save(notification)

            val notifications = repository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(
                    quantumId,
                    now.minusDays(1).atStartOfDay(),
                    now.plusDays(1).atStartOfDay())
            assertThat(notifications).isEmpty()
        }

        @Test
        fun `Should return count of 0 matches`() {
            val date = now.plusDays(3).atStartOfDay()

            val notifications = repository.countAllByQuantumIdIgnoreCaseAndShiftDateAndShiftTypeIgnoreCaseAndShiftModified(
                    "XYZ",
                    date.toLocalDate(),
                    ShiftNotificationType.SHIFT.value,
                    date)
            assertThat(notifications).isEqualTo(0)
        }

        @Test
        fun `Should return count if 1 matches case insensitive quantum_id`() {
            val date = now.plusDays(3).atStartOfDay()
            val notification = getValidShiftNotification(date, date)
            repository.save(notification)

            val notifications = repository.countAllByQuantumIdIgnoreCaseAndShiftDateAndShiftTypeIgnoreCaseAndShiftModified(
                    "XyZ",
                    date.toLocalDate(),
                    ShiftNotificationType.SHIFT.value,
                    date)
            assertThat(notifications).isEqualTo(1)
        }

        @Test
        fun `Should return count if 1 matches case insensitive shift_type`() {
            val date = now.plusDays(3).atStartOfDay()
            val notification = getValidShiftNotification(date, date)
            repository.save(notification)

            val notifications = repository.countAllByQuantumIdIgnoreCaseAndShiftDateAndShiftTypeIgnoreCaseAndShiftModified(
                    "XYZ",
                    date.toLocalDate(),
                    "ShIfT",
                    date)
            assertThat(notifications).isEqualTo(1)
        }

        @Test
        fun `Should return count if 1 matches`() {
            val date = now.plusDays(3).atStartOfDay()
            val notification = getValidShiftNotification(date, date)
            repository.save(notification)

            val notifications = repository.countAllByQuantumIdIgnoreCaseAndShiftDateAndShiftTypeIgnoreCaseAndShiftModified(
                    "XYZ",
                    date.toLocalDate(),
                    ShiftNotificationType.SHIFT.value,
                    date)
            assertThat(notifications).isEqualTo(1)
        }


        @Test
        fun `Should return count if 2 matches`() {
            val date = now.plusDays(3).atStartOfDay()
            val notification = getValidShiftNotification(date, date)
            repository.saveAll(listOf(notification, notification))

            val notifications = repository.countAllByQuantumIdIgnoreCaseAndShiftDateAndShiftTypeIgnoreCaseAndShiftModified(
                    "XYZ",
                    date.toLocalDate(),
                    ShiftNotificationType.SHIFT.value,
                    date)
            assertThat(notifications).isEqualTo(2)
        }

        @Test
        fun `Add Type Check Should return count of 0 matches`() {
            val date = now.plusDays(3).atStartOfDay()

            val notifications = repository.countAllByQuantumIdIgnoreCaseAndShiftDateAndShiftTypeIgnoreCaseAndActionTypeIgnoreCase(
                    "XYZ",
                    date.toLocalDate(),
                    ShiftNotificationType.SHIFT.value,
                    ShiftActionType.ADD.value)
            assertThat(notifications).isEqualTo(0)
        }

        @Test
        fun `Add Type Check Should return count if 1 matches`() {
            val date = now.plusDays(3).atStartOfDay()
            val notification = getValidShiftNotification(date, date)
            repository.save(notification)

            val notifications = repository.countAllByQuantumIdIgnoreCaseAndShiftDateAndShiftTypeIgnoreCaseAndActionTypeIgnoreCase(
                    "XYZ",
                    date.toLocalDate(),
                    ShiftNotificationType.SHIFT.value,
                    ShiftActionType.ADD.value)
            assertThat(notifications).isEqualTo(1)
        }


        @Test
        fun `Add Type Check Should return count if 2 matches`() {
            val date = now.plusDays(3).atStartOfDay()
            val notification = getValidShiftNotification(date, date)
            repository.saveAll(listOf(notification, notification))

            val notifications = repository.countAllByQuantumIdIgnoreCaseAndShiftDateAndShiftTypeIgnoreCaseAndActionTypeIgnoreCase(
                    "XYZ",
                    date.toLocalDate(),
                    ShiftNotificationType.SHIFT.value,
                    ShiftActionType.ADD.value)
            assertThat(notifications).isEqualTo(2)
        }
    }

    @Nested
    @DisplayName("Get Unprocessed Notification tests")
    inner class GetUnsentNotificationTests {

        @Test
        fun `Should only return unprocessed notifications`() {
            val firstDate = now.plusDays(3).atStartOfDay()
            val unProcessedNotification = getValidShiftNotification(firstDate, firstDate)
            repository.save(unProcessedNotification)

            val notifications = repository.findAllByProcessedIsFalse().toList()

            // Only one gets returned
            assertThat(notifications).hasSize(1)

            // Basic check that it's the unprocessed one
            assertThat(notifications[0].shiftModified).isEqualTo(firstDate)
        }

        @Test
        fun `Should only return unprocessed notifications when there are processed ones too`() {
            val firstDate = now.plusDays(3).atStartOfDay()
            val unProcessedNotification = getValidShiftNotification(firstDate, firstDate)
            repository.save(unProcessedNotification)

            val secondDate = now.plusDays(5).atStartOfDay()
            val processedNotification = getValidShiftNotification(secondDate, secondDate, true)
            repository.save(processedNotification)

            val notifications = repository.findAllByProcessedIsFalse().toList()

            // Only one gets returned
            assertThat(notifications).hasSize(1)

            // Basic check that it's the unprocessed one
            assertThat(notifications[0].shiftModified).isEqualTo(firstDate)
        }

        @Test
        fun `Should not return any unprocessed notifications when there are none`() {
            val firstDate = now.plusDays(3).atStartOfDay()
            val processedNotification = getValidShiftNotification(firstDate, firstDate, true)
            repository.save(processedNotification)

            val notifications = repository.findAllByProcessedIsFalse().toList()

            // none gets returned
            assertThat(notifications).hasSize(0)

        }
    }

    companion object {
        fun getValidShiftNotification(shiftDate: LocalDateTime, shiftModified: LocalDateTime, processed: Boolean = false): ShiftNotification {
            val quantumId = "XYZ"
            val taskStart = 123L
            val taskEnd = 456L
            val task = "Any Activity"
            val shiftType = ShiftNotificationType.SHIFT.value
            val actionType = ShiftActionType.ADD.value

            return ShiftNotification(
                    1L,
                    quantumId,
                    shiftDate.toLocalDate(),
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