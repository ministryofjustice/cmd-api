package uk.gov.justice.digital.hmpps.cmd.api.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.cmd.api.model.ShiftTaskNotification
import java.time.LocalDate
import java.time.LocalDateTime

@ActiveProfiles("test")
@DataJpaTest
class ShiftTaskNotificationRepositoryTest(
        @Autowired val repository: ShiftTaskNotificationRepository
) {

    private val now: LocalDate = LocalDate.now()

    @BeforeEach
    fun resetAllMocks() {
        repository.deleteAll()
    }

    @Nested
    @DisplayName("Get Shift Task Notification tests")
    inner class GetShiftTaskNotificationTests {

        @Test
        fun `Should return a notification between the dates`() {
            val quantumId = "XYZ"
            val date = now.atStartOfDay()
            val notification = getValidShiftTaskNotification(date, date)
            repository.save(notification)

            val notifications = repository.findAllByQuantumIdAndLastModifiedDateTimeIsBetween(
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
            val notification = getValidShiftTaskNotification(date, date)
            repository.save(notification)

            val notifications = repository.findAllByQuantumIdAndLastModifiedDateTimeIsBetween(
                    quantumId,
                    now.minusDays(1).atStartOfDay(),
                    now.plusDays(1).atStartOfDay())
            assertThat(notifications).isEmpty()
        }

        @Test
        fun `Should not return a notification later than between the dates`() {
            val quantumId = "XYZ"
            val date = now.plusDays(3).atStartOfDay()
            val notification = getValidShiftTaskNotification(date, date)
            repository.save(notification)

            val notifications = repository.findAllByQuantumIdAndLastModifiedDateTimeIsBetween(
                    quantumId,
                    now.minusDays(1).atStartOfDay(),
                    now.plusDays(1).atStartOfDay())
            assertThat(notifications).isEmpty()
        }
    }

    companion object {

        fun getValidShiftTaskNotification(taskDate: LocalDateTime, lastModified: LocalDateTime): ShiftTaskNotification {

            val quantumId = "XYZ"
            val description = "Any Description,"
            val taskStartSec = 123
            val taskEndSec = 456
            val activity = "Any Activity"
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