package uk.gov.justice.digital.hmpps.cmd.api.scheduler

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrModifiedDetailDto
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrRegionSelectorService
import uk.gov.justice.digital.hmpps.cmd.api.controllers.ResourceTest
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailModificationType
import uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.cmd.api.model.Notification
import uk.gov.justice.digital.hmpps.cmd.api.repository.NotificationRepository
import uk.gov.justice.digital.hmpps.prisonertonomisupdate.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.prisonertonomisupdate.wiremock.PrisonApiExtension
import java.time.LocalDateTime

@ExtendWith(PrisonApiExtension::class, HmppsAuthApiExtension::class)
@DisplayName("Integration Tests for Polling Scheduler")
class PollingSchedulerIntegrationTest(
  @Autowired val pollingScheduler: PollingScheduler,
  @Autowired val notificationRepository: NotificationRepository,
  @Autowired @MockitoBean private val csrRegionSelectorService: CsrRegionSelectorService,
) : ResourceTest() {

  @BeforeEach
  fun init() {
    notificationRepository.deleteAll()
  }

  @Test
  fun `updates are processed`() {
    whenever(csrRegionSelectorService.getModified(1)).thenReturn(
      listOf(
        CsrModifiedDetailDto(
          id = 101,
          quantumId = A_USER,
          shiftModified = LocalDateTime.parse("2022-03-25T15:00:00"),
          shiftType = ShiftType.SHIFT,
          detailStart = LocalDateTime.parse("2022-03-31T10:00:00"),
          detailEnd = LocalDateTime.parse("2022-03-31T11:00:00"),
          activity = "CCTV monitoring",
          actionType = DetailModificationType.ADD,
        ),
        CsrModifiedDetailDto(
          id = 103,
          quantumId = null,
          shiftModified = LocalDateTime.parse("2022-03-25T15:30:00"),
          shiftType = ShiftType.SHIFT,
          detailStart = LocalDateTime.parse("2022-03-31T10:00:00"),
          detailEnd = LocalDateTime.parse("2022-03-31T11:00:00"),
          activity = "CCTV monitoring",
          actionType = DetailModificationType.ADD,
        ),
      ),
    )
    whenever(csrRegionSelectorService.getModified(2)).thenReturn(
      listOf(
        CsrModifiedDetailDto(
          id = 102,
          quantumId = "other-user",
          shiftModified = LocalDateTime.parse("2022-03-25T15:40:00"),
          shiftType = ShiftType.SHIFT,
          detailStart = LocalDateTime.parse("2022-03-31T10:00:00"),
          detailEnd = LocalDateTime.parse("2022-03-31T11:00:00"),
          activity = "CCTV monitoring",
          actionType = DetailModificationType.ADD,
        ),
      ),
    )
    whenever(csrRegionSelectorService.getModified(3)).thenReturn(emptyList())
    whenever(csrRegionSelectorService.getModified(4)).thenReturn(emptyList())
    whenever(csrRegionSelectorService.getModified(5)).thenReturn(emptyList())
    whenever(csrRegionSelectorService.getModified(6)).thenReturn(emptyList())

    pollingScheduler.pollNotifications()

    val saved = notificationRepository.findAll()
    assertThat(saved).containsExactly(
      Notification(
        // generated
        id = saved.first().id,
        quantumId = A_USER,
        shiftModified = LocalDateTime.parse("2022-03-25T15:00:00"),
        detailStart = LocalDateTime.parse("2022-03-31T10:00:00"),
        detailEnd = LocalDateTime.parse("2022-03-31T11:00:00"),
        activity = "CCTV monitoring",
        actionType = DetailModificationType.ADD,
        parentType = ShiftType.SHIFT,
        processed = true,
      ),
      Notification(
        id = saved.last().id,
        quantumId = "other-user",
        shiftModified = LocalDateTime.parse("2022-03-25T15:40:00"),
        detailStart = LocalDateTime.parse("2022-03-31T10:00:00"),
        detailEnd = LocalDateTime.parse("2022-03-31T11:00:00"),
        activity = "CCTV monitoring",
        actionType = DetailModificationType.ADD,
        parentType = ShiftType.SHIFT,
        processed = true,
      ),
    )
    verify(csrRegionSelectorService).deleteProcessed(listOf(101, 103), 1)
    verify(csrRegionSelectorService).deleteProcessed(listOf(102), 2)
    verify(csrRegionSelectorService, never()).deleteProcessed(any(), eq(3))
    verify(csrRegionSelectorService, never()).deleteProcessed(any(), eq(4))
    verify(csrRegionSelectorService, never()).deleteProcessed(any(), eq(5))
    verify(csrRegionSelectorService, never()).deleteProcessed(any(), eq(6))
  }

  @Test
  fun `tidy job functions`() {
    notificationRepository.save(
      Notification(
        quantumId = "user1",
        shiftModified = LocalDateTime.now().minusMonths(4),
        detailStart = LocalDateTime.parse("2022-03-31T10:00:00"),
        detailEnd = LocalDateTime.parse("2022-03-31T11:00:00"),
        activity = "CCTV monitoring",
        actionType = DetailModificationType.ADD,
        parentType = ShiftType.SHIFT,
        processed = true,
      ),
    )
    notificationRepository.save(
      Notification(
        quantumId = "user2",
        shiftModified = LocalDateTime.now().minusMonths(2),
        detailStart = LocalDateTime.parse("2022-03-31T10:00:00"),
        detailEnd = LocalDateTime.parse("2022-03-31T11:00:00"),
        activity = "CCTV monitoring",
        actionType = DetailModificationType.ADD,
        parentType = ShiftType.SHIFT,
        processed = true,
      ),
    )

    pollingScheduler.tidyNotifications()

    val all = notificationRepository.findAll()
    assertThat(all).hasSize(1)
    assertThat(all.first().quantumId).isEqualTo("user2")
  }

  companion object {
    private const val A_USER = "API_TEST_USER"
  }
}
