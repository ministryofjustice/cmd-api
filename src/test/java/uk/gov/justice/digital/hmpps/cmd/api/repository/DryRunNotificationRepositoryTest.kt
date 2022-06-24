package uk.gov.justice.digital.hmpps.cmd.api.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailModificationType
import uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.cmd.api.model.DryRunNotification
import java.time.LocalDate
import java.time.LocalDateTime

@ActiveProfiles("test")
@DataJpaTest
class DryRunNotificationRepositoryTest(
  @Autowired val repository: DryRunNotificationRepository
) {

  private val now: LocalDate = LocalDate.now()

  @BeforeEach
  fun resetAllMocks() {
    repository.deleteAll()
  }

  @Nested
  @DisplayName("Get shift notification tests")
  inner class GetNotificationTests {

    @Test
    fun `Should return a notification between the dates case insensitive quantum_id`() {
      val quantumId = "XyZ"
      val date = now.atStartOfDay()
      val notification = getValidNotification(date, date)
      repository.save(notification)

      val notifications = repository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(
        quantumId,
        now.minusDays(1).atStartOfDay(),
        now.plusDays(1).atStartOfDay()
      )
      assertThat(notifications).isNotEmpty

      assertThat(notifications.contains(notification))
    }

    @Test
    fun `Should return a notification between the dates`() {
      val quantumId = "XYZ"
      val date = now.atStartOfDay()
      val notification = getValidNotification(date, date)
      repository.save(notification)

      val notifications = repository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(
        quantumId,
        now.minusDays(1).atStartOfDay(),
        now.plusDays(1).atStartOfDay()
      )
      assertThat(notifications).isNotEmpty

      assertThat(notifications.contains(notification))
    }

    @Test
    fun `Should not return a notification earlier than between the dates`() {
      val quantumId = "XYZ"
      val date = now.minusDays(3).atStartOfDay()
      val notification = getValidNotification(date, date)
      repository.save(notification)

      val notifications = repository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(
        quantumId,
        now.minusDays(1).atStartOfDay(),
        now.plusDays(1).atStartOfDay()
      )
      assertThat(notifications).isEmpty()
    }

    @Test
    fun `Should not return a notification later than between the dates`() {
      val quantumId = "XYZ"
      val date = now.plusDays(3).atStartOfDay()
      val notification = getValidNotification(date, date)
      repository.save(notification)

      val notifications = repository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(
        quantumId,
        now.minusDays(1).atStartOfDay(),
        now.plusDays(1).atStartOfDay()
      )
      assertThat(notifications).isEmpty()
    }

    @Test
    fun `Should return count of 0 matches`() {
      val date = now.plusDays(3).atStartOfDay()

      val notifications = repository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
        "XYZ",
        date,
        ShiftType.SHIFT,
        date
      )
      assertThat(notifications).isEqualTo(0)
    }

    @Test
    fun `Should return count if 1 matches case insensitive quantum_id`() {
      val date = now.plusDays(3).atStartOfDay()
      val notification = getValidNotification(date, date)
      repository.save(notification)

      val notifications = repository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
        "XyZ",
        date,
        ShiftType.SHIFT,
        date
      )
      assertThat(notifications).isEqualTo(1)
    }

    @Test
    fun `Should return count if 1 matches`() {
      val date = now.plusDays(3).atStartOfDay()
      val notification = getValidNotification(date, date)
      repository.save(notification)

      val notifications = repository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
        "XYZ",
        date,
        ShiftType.SHIFT,
        date
      )
      assertThat(notifications).isEqualTo(1)
    }

    @Test
    fun `Should return count if 2 matches`() {
      val date = now.plusDays(3).atStartOfDay()
      val notification = getValidNotification(date, date)
      repository.saveAll(listOf(notification, notification))

      val notifications = repository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
        "XYZ",
        date,
        ShiftType.SHIFT,
        date
      )
      assertThat(notifications).isEqualTo(2)
    }

    @Test
    fun `Add Type Check Should return count of 0 matches`() {
      val date = now.plusDays(3).atStartOfDay()

      val notifications = repository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndActionType(
        "XYZ",
        date,
        ShiftType.SHIFT,
        DetailModificationType.ADD
      )
      assertThat(notifications).isEqualTo(0)
    }

    @Test
    fun `Add Type Check Should return count if 1 matches`() {
      val date = now.plusDays(3).atStartOfDay()
      val notification = getValidNotification(date, date)
      repository.save(notification)

      val notifications = repository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndActionType(
        "XYZ",
        date,
        ShiftType.SHIFT,
        DetailModificationType.ADD
      )
      assertThat(notifications).isEqualTo(1)
    }

    @Test
    fun `Add Type Check Should return count if 2 matches`() {
      val date = now.plusDays(3).atStartOfDay()
      val notification = getValidNotification(date, date)
      repository.saveAll(listOf(notification, notification))

      val notifications = repository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndActionType(
        "XYZ",
        date,
        ShiftType.SHIFT,
        DetailModificationType.ADD
      )
      assertThat(notifications).isEqualTo(2)
    }
  }

  @Nested
  @DisplayName("Get Unprocessed Notification tests")
  inner class GetUnsentNotificationTests {

    @Test
    fun `Should only return unprocessed notifications`() {
      val firstDate = now.plusDays(3).atStartOfDay()
      val unProcessedNotification = getValidNotification(firstDate, firstDate)
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
      val unProcessedNotification = getValidNotification(firstDate, firstDate)
      repository.save(unProcessedNotification)

      val secondDate = now.plusDays(5).atStartOfDay()
      val processedNotification = getValidNotification(secondDate, secondDate, true)
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
      val processedNotification = getValidNotification(firstDate, firstDate, true)
      repository.save(processedNotification)

      val notifications = repository.findAllByProcessedIsFalse().toList()

      // none gets returned
      assertThat(notifications).hasSize(0)
    }
  }

  companion object {
    fun getValidNotification(shiftDate: LocalDateTime, shiftModified: LocalDateTime, processed: Boolean = false): DryRunNotification {
      val quantumId = "XYZ"
      val task = "Any Activity"
      val shiftType = ShiftType.SHIFT
      val actionType = DetailModificationType.ADD

      return DryRunNotification(
        id = 1L,
        quantumId = quantumId,
        shiftModified = shiftModified,
        detailStart = shiftDate,
        detailEnd = shiftDate,
        activity = task,
        parentType = shiftType,
        actionType = actionType,
        processed = processed
      )
    }
  }
}
