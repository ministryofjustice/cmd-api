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
    @DisplayName("Get Shift Notification tests")
    inner class GetShiftNotificationTests {

        @Test
        fun `Should return a notification between the dates`() {
            val quantumId = "XYZ"
            val date = now.atStartOfDay()
            val notification = getValidShiftNotification(date, date)
            repository.save(notification)

            val notifications = repository.findAllByQuantumIdAndLastModifiedIsBetween(
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

            val notifications = repository.findAllByQuantumIdAndLastModifiedIsBetween(
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

            val notifications = repository.findAllByQuantumIdAndLastModifiedIsBetween(
                    quantumId,
                    now.minusDays(1).atStartOfDay(),
                    now.plusDays(1).atStartOfDay())
            assertThat(notifications).isEmpty()
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
            assertThat(notifications[0].lastModified).isEqualTo(firstDate)
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
            assertThat(notifications[0].lastModified).isEqualTo(firstDate)
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
        fun getValidShiftNotification(shiftDate: LocalDateTime, lastModified: LocalDateTime, processed: Boolean = false): ShiftNotification {

            val quantumId = "XYZ"
            val description = "Any Description,"
            val notificationType = 0L

            return ShiftNotification(
                    quantumId,
                    description,
                    shiftDate,
                    lastModified,
                    notificationType,
                    processed
            )
        }
    }

}