package uk.gov.justice.digital.hmpps.cmd.api.service

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrClient
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrModifiedDetailDto
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailModificationType
import uk.gov.justice.digital.hmpps.cmd.api.model.DryRunNotification
import uk.gov.justice.digital.hmpps.cmd.api.model.Prison
import uk.gov.justice.digital.hmpps.cmd.api.repository.DryRunNotificationRepository
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import uk.gov.service.notify.NotificationClient
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@ExtendWith(MockKExtension::class)
@DisplayName("DryRunNotification Service tests Generate Shift")
internal class DryRunNotificationServiceTest_Generate_Shift {
  private val dryRunNotificationRepository: DryRunNotificationRepository = mockk(relaxUnitFun = true)
  private val userPreferenceService: UserPreferenceService = mockk(relaxUnitFun = true)
  private val prisonService: PrisonService = mockk(relaxUnitFun = true)
  private val authenticationFacade: AuthenticationFacade = mockk(relaxUnitFun = true)
  private val notifyClient: NotificationClient = mockk(relaxUnitFun = true)
  private val csrClient: CsrClient = mockk(relaxUnitFun = true)
  private val clock = Clock.fixed(Instant.parse("2022-04-01T10:00:00Z"), ZoneId.systemDefault())
  private val service = DryRunNotificationService(
    dryRunNotificationRepository,
    userPreferenceService,
    clock,
    authenticationFacade,
    3,
    notifyClient,
    prisonService,
    csrClient,
  )

  private val today = LocalDate.now(clock).atStartOfDay()

  @BeforeEach
  fun resetAllMocks() {
    clearMocks(dryRunNotificationRepository)
    clearMocks(userPreferenceService)
    clearMocks(notifyClient)
    clearMocks(csrClient)
  }

  @Nested
  @DisplayName("Generate and save Notification tests")
  inner class GenerateAndSaveNotificationTests {

    @BeforeEach
    fun `set up prison fetching`() {
      val prison1 = Prison("ABC", "Main Gate", "Midgar Central", 1)
      every { prisonService.getAllPrisons() } returns listOf(prison1)
    }

    @Test
    fun `Should disregard Shift Notification if it exists in our db`() {

      val quantumId = "CSTRIFE_GEN"
      val shiftDate = today.plusDays(2).toLocalDate()
      val start = shiftDate.atStartOfDay().plusSeconds(123L)
      val end = shiftDate.atStartOfDay().plusSeconds(456L)
      val task = "Guard Duty"
      val shiftType = ShiftType.SHIFT
      val dto1 = CsrModifiedDetailDto(
        id = 1,
        quantumId = quantumId,
        shiftModified = today,
        shiftType = shiftType,
        detailStart = start,
        detailEnd = end,
        activity = task,
        actionType = DetailModificationType.ADD
      )

      every { csrClient.getModified(1) } returns listOf(dto1)

      every {
        dryRunNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          start,
          shiftType,
          today
        )
      } returns 1

      service.dryRunNotifications(1)

      verify(exactly = 0) { dryRunNotificationRepository.saveAll<DryRunNotification>(allAny()) }
    }

    @Test
    fun `Should add multiple Shift notifications for one user`() {

      val quantumId = "CSTRIFE_GEN"
      val start = today.plusSeconds(123L)
      val end = today.plusSeconds(456L)
      val task = "Guard Duty"
      val shiftType = ShiftType.SHIFT
      val dto1 = CsrModifiedDetailDto(
        1,
        quantumId,
        today,
        shiftType,
        start.plusDays(1),
        end.plusDays(1),
        task,
        DetailModificationType.ADD
      )

      val dto2 = CsrModifiedDetailDto(
        2,
        quantumId,
        today,
        shiftType,
        start.plusDays(2),
        end.plusDays(2),
        task,
        DetailModificationType.ADD
      )

      val dto3 = CsrModifiedDetailDto(
        3,
        quantumId,
        today,
        shiftType,
        start.plusDays(3),
        end.plusDays(3),
        task,
        DetailModificationType.ADD
      )

      every { csrClient.getModified(any()) } returns listOf(dto1, dto2, dto3)

      every {
        dryRunNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          any(),
          shiftType,
          today
        )
      } returns 0

      val results = mutableListOf<Collection<DryRunNotification>>()
      every { dryRunNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.dryRunNotifications(1)
      val notification1 = DryRunNotification(
        0,
        quantumId,
        today,
        start.plusDays(1),
        end.plusDays(1),
        task,
        shiftType,
        DetailModificationType.ADD,
        false
      )
      val notification2 = DryRunNotification(
        0,
        quantumId,
        today,
        start.plusDays(2),
        end.plusDays(2),
        task,
        shiftType,
        DetailModificationType.ADD,
        false
      )
      val notification3 = DryRunNotification(
        0,
        quantumId,
        today,
        start.plusDays(3),
        end.plusDays(3),
        task,
        shiftType,
        DetailModificationType.ADD,
        false
      )
      assertThat(results[0]).isEqualTo(listOf(notification1, notification2, notification3))
    }

    @Test
    fun `Should not add multiple duplicate Shift notifications for one user`() {

      val quantumId = "CSTRIFE_GEN"
      val start = LocalDateTime.now(clock)
      val end = LocalDateTime.now(clock)
      val task = "Guard Duty"
      val shiftType = ShiftType.SHIFT
      val dto1 = CsrModifiedDetailDto(
        1,
        quantumId,
        today,
        shiftType,
        start,
        end,
        task,
        DetailModificationType.ADD
      )
      val dto2 = dto1.copy(id = 2L)
      val dto3 = dto1.copy(id = 3L)

      every { csrClient.getModified(any()) } returns listOf(dto1, dto2, dto3)

      every {
        dryRunNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          any(),
          shiftType,
          today
        )
      } returns 0

      val results = mutableListOf<Collection<DryRunNotification>>()
      every { dryRunNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.dryRunNotifications(1)
      val notification1 =
        DryRunNotification(0, quantumId, today, start, end, task, shiftType, DetailModificationType.ADD, false)
      assertThat(results[0]).isEqualTo(listOf(notification1))
    }

    @Test
    fun `Should filter multiple notifications for same shift with different modified times for one user`() {

      val quantumId = "CSTRIFE_GEN"
      val start = today.plusSeconds(123L)
      val end = today.plusSeconds(456L)
      val task = "Guard Duty"
      val shiftType = ShiftType.SHIFT
      val dto1 = CsrModifiedDetailDto(1, quantumId, today, shiftType, start, end, task, DetailModificationType.ADD)
      val dto2 = dto1.copy(id = 2, shiftModified = today.plusSeconds(5))
      val dto3 = dto1.copy(id = 3, shiftModified = today.plusSeconds(10))
      every { csrClient.getModified(any()) } returns listOf(dto1, dto2, dto3)

      every {
        dryRunNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          any(),
          shiftType,
          today
        )
      } returns 0

      val results = mutableListOf<Collection<DryRunNotification>>()
      every { dryRunNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.dryRunNotifications(1)
      val notification1 =
        DryRunNotification(0, quantumId, today, start, end, task, shiftType, DetailModificationType.ADD, false)

      assertThat(results[0]).isEqualTo(listOf(notification1))
    }

    @Test
    fun `Should not save edit Shift notification types, these are covered by shift Task Notifications`() {

      val quantumId = "CSTRIFE_GEN"
      val task = null
      val shiftType = ShiftType.SHIFT
      val dto1 = CsrModifiedDetailDto(
        1,
        quantumId,
        today,
        shiftType,
        today,
        today,
        task,
        DetailModificationType.EDIT
      )

      every { csrClient.getModified(any()) } returns listOf(dto1)

      every {
        dryRunNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndActionType(
          quantumId,
          today,
          shiftType,
          DetailModificationType.ADD
        )
      } returns 1
      every {
        dryRunNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          today,
          shiftType,
          today
        )
      } returns 0

      service.dryRunNotifications(1)

      verify(exactly = 0) { dryRunNotificationRepository.saveAll<DryRunNotification>(allAny()) }
    }

    @Test
    fun `Should save Add shift notification types if not exist in the DB`() {

      val quantumId = "CSTRIFE_GEN"
      val task = "Guard Duty"
      val shiftType = ShiftType.SHIFT
      val dto1 = CsrModifiedDetailDto(
        1,
        quantumId,
        today,
        shiftType,
        today,
        today,
        task,
        DetailModificationType.ADD
      )

      every { csrClient.getModified(any()) } returns listOf(dto1)

      every {
        dryRunNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          today,
          shiftType,
          today
        )
      } returns 0

      val results = mutableListOf<Collection<DryRunNotification>>()
      every { dryRunNotificationRepository.saveAll(capture(results)) } returns listOf()
      service.dryRunNotifications(1)

      val expected =
        DryRunNotification(0, quantumId, today, today, today, task, shiftType, DetailModificationType.ADD, false)
      assertThat(results[0]).isEqualTo(listOf(expected))
      verify { csrClient.deleteProcessed(1, listOf(1L)) }
    }

    @Test
    fun `Should save remove Shift notification types if not exist in the DB`() {
      val quantumId = "CSTRIFE_GEN"
      val task = "Guard Duty"
      val shiftType = ShiftType.SHIFT
      val dto1 = CsrModifiedDetailDto(
        1,
        quantumId,
        today,
        shiftType,
        today,
        today,
        task,
        DetailModificationType.DELETE
      )

      every { csrClient.getModified(any()) } returns listOf(dto1)

      every {
        dryRunNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          today,
          shiftType,
          today
        )
      } returns 0

      val results = mutableListOf<Collection<DryRunNotification>>()
      every { dryRunNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.dryRunNotifications(1)

      val expected =
        DryRunNotification(0, quantumId, today, today, today, task, shiftType, DetailModificationType.DELETE, false)
      assertThat(results[0]).isEqualTo(listOf(expected))
    }

    @Test
    fun `Should disregard unprocessed Shift Notification duplicates`() {

      val quantumId = "CSTRIFE_GEN"
      val shiftDate = today.plusDays(2).toLocalDate()
      val start = shiftDate.atStartOfDay().plusSeconds(123L)
      val end = shiftDate.atStartOfDay().plusSeconds(456L)
      val task = "Guard Duty"
      val shiftType = ShiftType.SHIFT
      val dto1 = CsrModifiedDetailDto(
        1,
        quantumId,
        today,
        shiftType,
        start,
        end,
        task,
        DetailModificationType.ADD
      )

      every { csrClient.getModified(any()) } returns listOf(dto1)

      every {
        dryRunNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          start,
          shiftType,
          today
        )
      } returns 1

      service.dryRunNotifications(1)

      verify(exactly = 0) { dryRunNotificationRepository.saveAll<DryRunNotification>(allAny()) }
    }

    @Test
    fun `Should do nothing if there is nothing to do`() {
      every { csrClient.getModified(any()) } returns listOf()

      service.dryRunNotifications(1)

      verify(exactly = 0) { dryRunNotificationRepository.saveAll<DryRunNotification>(allAny()) }
    }

    @Test
    fun `Should Change an Edit with no existing Add in the database to an Edit`() {

      val quantumId = "CSTRIFE_GEN"
      val task = null
      val shiftType = ShiftType.SHIFT
      val dto1 = CsrModifiedDetailDto(
        1,
        quantumId,
        today,
        shiftType,
        today,
        today,
        task,
        DetailModificationType.EDIT
      )

      every { csrClient.getModified(any()) } returns listOf(dto1, dto1)

      every {
        dryRunNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndActionType(
          quantumId,
          today,
          shiftType,
          DetailModificationType.ADD
        )
      } returns 0
      every {
        dryRunNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          today,
          shiftType,
          today
        )
      } returns 0

      val results = mutableListOf<Collection<DryRunNotification>>()
      every { dryRunNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.dryRunNotifications(1)

      val expected =
        DryRunNotification(0, quantumId, today, today, today, task, shiftType, DetailModificationType.ADD, false)
      assertThat(results[0]).isEqualTo(listOf(expected))
    }

    @Test
    fun `Should ignore user with changes that are too recent`() {

      val now = LocalDateTime.now(clock)
      val start = today.plusSeconds(123L)
      val end = today.plusSeconds(456L)
      val shiftType = ShiftType.SHIFT

      val dto1 = CsrModifiedDetailDto(
        1, "FINISHED_USER", now.minusMinutes(7), shiftType, start, end, "Guard Duty", DetailModificationType.ADD
      )
      val dto2 = dto1.copy(id = 2, shiftModified = now.minusMinutes(6), quantumId = "STILL_CHANGING_USER")
      val dto3 =
        dto1.copy(id = 3, shiftModified = now.minusMinutes(6), quantumId = "FINISHED_USER", activity = "another")
      val dto4 = dto1.copy(id = 4, shiftModified = now.minusMinutes(4), quantumId = "STILL_CHANGING_USER")

      every { csrClient.getModified(any()) } returns listOf(dto1, dto2, dto3, dto4)

      every {
        dryRunNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          "FINISHED_USER",
          any(),
          shiftType,
          any()
        )
      } returns 0

      val results = mutableListOf<Collection<DryRunNotification>>()
      every { dryRunNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.dryRunNotifications(1)

      val notification1 = DryRunNotification(
        0,
        "FINISHED_USER",
        now.minusMinutes(7),
        start,
        end,
        "Guard Duty",
        shiftType,
        DetailModificationType.ADD,
        false
      )
      val notification3 = DryRunNotification(
        0,
        "FINISHED_USER",
        now.minusMinutes(6),
        start,
        end,
        "another",
        shiftType,
        DetailModificationType.ADD,
        false
      )
      assertThat(results[0]).asList().containsExactly(notification1, notification3)
      verify { csrClient.deleteProcessed(1, listOf(1L, 3L)) }
    }

    @Test
    fun `Should process but not notify when null quantum id or lastmodified`() {

      val quantumId = "CSTRIFE_GEN"
      val shiftDate = today.plusDays(2).toLocalDate()
      val start = shiftDate.atStartOfDay().plusSeconds(123L)
      val end = shiftDate.atStartOfDay().plusSeconds(456L)
      val task = "Guard Duty"
      val shiftType = ShiftType.SHIFT
      val dto1 = CsrModifiedDetailDto(
        1,
        quantumId,
        today,
        shiftType,
        start,
        end,
        task,
        DetailModificationType.ADD
      )
      val dto2 = dto1.copy(id = 2, quantumId = null)
      val dto3 = dto1.copy(id = 3, shiftModified = null)
      val dto4 = dto1.copy(id = 4, quantumId = null, shiftModified = null)

      every { csrClient.getModified(any()) } returns listOf(dto1, dto2, dto3, dto4)

      every {
        dryRunNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
          quantumId,
          start,
          shiftType,
          today
        )
      } returns 0

      val results = mutableListOf<Collection<DryRunNotification>>()
      every { dryRunNotificationRepository.saveAll(capture(results)) } returns listOf()

      service.dryRunNotifications(1)

      assertThat(results[0]).asList().hasSize(1)
      verify { csrClient.deleteProcessed(1, listOf(1L, 2L, 3L, 4L)) }
    }
  }
}
