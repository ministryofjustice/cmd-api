package uk.gov.justice.digital.hmpps.cmd.api.service

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrClient
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrModifiedDetailDto
import uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftActionType
import uk.gov.justice.digital.hmpps.cmd.api.model.Prison
import uk.gov.justice.digital.hmpps.cmd.api.model.ShiftNotification
import uk.gov.justice.digital.hmpps.cmd.api.repository.ShiftNotificationRepository
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import uk.gov.service.notify.NotificationClient
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@ExtendWith(MockKExtension::class)
@DisplayName("Notification Service tests Generate Shift")
internal class NotificationServiceTest_Generate_Shift {
    private val shiftNotificationRepository: ShiftNotificationRepository = mockk(relaxUnitFun = true)
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

    @BeforeEach
    fun resetAllMocks() {
        clearMocks(shiftNotificationRepository)
        clearMocks(userPreferenceService)
        clearMocks(notifyClient)
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
            val today = LocalDateTime.now(clock)
            val quantumId = "CSTRIFE_GEN"
            val shiftDate = today.plusDays(2).toLocalDate()
            val start = 123L
            val end = 456L
            val task = "Guard Duty"
            val shiftType = "Shift"
            val dto1 = CsrModifiedDetailDto(
                    quantumId,
                    today,
                    shiftDate,
                    shiftType,
                    start,
                    end,
                    task,
                    ShiftActionType.ADD.value
            )

            every { csrClient.getModifiedDetails(any(), any()) } returns listOf(dto1)

            every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndShiftDateAndShiftTypeIgnoreCaseAndShiftModified(quantumId, shiftDate, shiftType, today) } returns 1

            val results = mutableListOf<Collection<ShiftNotification>>()
            every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()

            service.refreshNotifications()

            assertThat(results[0]).isEqualTo(listOf<ShiftNotification>())
        }

        @Test
        fun `Should add multiple Shift notifications for one user`() {
            val today = LocalDateTime.now(clock)
            val quantumId = "CSTRIFE_GEN"
            val start = 123L
            val end = 456L
            val task = "Guard Duty"
            val shiftType = "Shift"
            val dto1 = CsrModifiedDetailDto(
                    quantumId,
                    today,
                    today.plusDays(1).toLocalDate(),
                    shiftType,
                    start,
                    end,
                    task,
                    ShiftActionType.ADD.value
            )

            val dto2 = CsrModifiedDetailDto(
                    quantumId,
                    today,
                    today.plusDays(2).toLocalDate(),
                    shiftType,
                    start,
                    end,
                    task,
                    ShiftActionType.ADD.value
            )

            val dto3 = CsrModifiedDetailDto(
                    quantumId,
                    today,
                    today.plusDays(3).toLocalDate(),
                    shiftType,
                    start,
                    end,
                    task,
                    ShiftActionType.ADD.value
            )

            every { csrClient.getModifiedDetails(any(), any()) } returns listOf(dto1, dto2, dto3)

            every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndShiftDateAndShiftTypeIgnoreCaseAndShiftModified(quantumId, any(), shiftType, today) } returns 0

            val results = mutableListOf<Collection<ShiftNotification>>()
            every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()

            service.refreshNotifications()
            val notification1 = ShiftNotification(null, quantumId, today.plusDays(1).toLocalDate(), today, start, end, task, shiftType, ShiftActionType.ADD.value, false)
            val notification2 = ShiftNotification(null, quantumId, today.plusDays(2).toLocalDate(), today, start, end, task, shiftType, ShiftActionType.ADD.value, false)
            val notification3 = ShiftNotification(null, quantumId, today.plusDays(3).toLocalDate(), today, start, end, task, shiftType, ShiftActionType.ADD.value, false)
            assertThat(results[0]).isEqualTo(listOf(notification1, notification2, notification3))
        }

        @Test
        fun `Should not add multiple duplicate Shift notifications for one user`() {
            val today = LocalDateTime.now(clock)
            val quantumId = "CSTRIFE_GEN"
            val start = null
            val end = null
            val task = "Guard Duty"
            val shiftType = "Shift"
            val dto1 = CsrModifiedDetailDto(
                    quantumId,
                    today,
                    today.toLocalDate(),
                    shiftType,
                    start,
                    end,
                    task,
                    ShiftActionType.ADD.value
            )

            val dto2 = CsrModifiedDetailDto(
                    quantumId,
                    today,
                    today.toLocalDate(),
                    shiftType,
                    start,
                    end,
                    task,
                    ShiftActionType.ADD.value
            )

            val dto3 = CsrModifiedDetailDto(
                    quantumId,
                    today,
                    today.toLocalDate(),
                    shiftType,
                    start,
                    end,
                    task,
                    ShiftActionType.ADD.value
            )

            every { csrClient.getModifiedDetails(any(), any()) } returns listOf(dto1, dto2, dto3)

            every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndShiftDateAndShiftTypeIgnoreCaseAndShiftModified(quantumId, any(), shiftType, today) } returns 0

            val results = mutableListOf<Collection<ShiftNotification>>()
            every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()

            service.refreshNotifications()
            val notification1 = ShiftNotification(null, quantumId, today.toLocalDate(), today, start, end, task, shiftType, ShiftActionType.ADD.value, false)
            assertThat(results[0]).isEqualTo(listOf(notification1))
        }

        @Test
        fun `Should add multiple notifications for same shift with different modified times for one user`() {
            val today = LocalDateTime.now(clock)
            val quantumId = "CSTRIFE_GEN"
            val start = 123L
            val end = 456L
            val task = "Guard Duty"
            val shiftType = "Shift"
            val dto1 = CsrModifiedDetailDto(
                    quantumId,
                    today,
                    today.toLocalDate(),
                    shiftType,
                    start,
                    end,
                    task,
                    ShiftActionType.ADD.value
            )

            val dto2 = CsrModifiedDetailDto(
                    quantumId,
                    today.plusSeconds(5),
                    today.toLocalDate(),
                    shiftType,
                    start,
                    end,
                    task,
                    ShiftActionType.ADD.value
            )

            val dto3 = CsrModifiedDetailDto(
                    quantumId,
                    today.plusSeconds(10),
                    today.toLocalDate(),
                    shiftType,
                    start,
                    end,
                    task,
                    ShiftActionType.ADD.value
            )

            every { csrClient.getModifiedDetails(any(), any()) } returns listOf(dto1, dto2, dto3)

            every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndShiftDateAndShiftTypeIgnoreCaseAndShiftModified(quantumId, any(), shiftType, today) } returns 0
            every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndShiftDateAndShiftTypeIgnoreCaseAndShiftModified(quantumId, any(), shiftType, today.plusSeconds(5)) } returns 0
            every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndShiftDateAndShiftTypeIgnoreCaseAndShiftModified(quantumId, any(), shiftType, today.plusSeconds(10)) } returns 0

            val results = mutableListOf<Collection<ShiftNotification>>()
            every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()

            service.refreshNotifications()
            val notification1 = ShiftNotification(null, quantumId, today.toLocalDate(), today, start, end, task, shiftType, ShiftActionType.ADD.value, false)
            val notification2 = ShiftNotification(null, quantumId, today.toLocalDate(), today.plusSeconds(5), start, end, task, shiftType, ShiftActionType.ADD.value, false)
            val notification3 = ShiftNotification(null, quantumId, today.toLocalDate(), today.plusSeconds(10), start, end, task, shiftType, ShiftActionType.ADD.value, false)
            assertThat(results[0]).isEqualTo(listOf(notification1,notification2,notification3))
        }

        @Test
        fun `Should not save edit Shift notification types, these are covered by shift Task Notifications`() {
            val today = LocalDateTime.now(clock)
            val quantumId = "CSTRIFE_GEN"
            val shiftDate = today.plusDays(2).toLocalDate()
            val start = null
            val end = null
            val task = "Guard Duty"
            val shiftType = "Shift"
            val dto1 = CsrModifiedDetailDto(
                    quantumId,
                    today,
                    shiftDate,
                    shiftType,
                    start,
                    end,
                    task,
                    ShiftActionType.EDIT.value
            )

            every { csrClient.getModifiedDetails(any(), any()) } returns listOf(dto1)

            every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndShiftDateAndShiftTypeIgnoreCaseAndActionTypeIgnoreCase(quantumId, shiftDate, shiftType, ShiftActionType.ADD.value) } returns 1
            every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndShiftDateAndShiftTypeIgnoreCaseAndShiftModified(quantumId, shiftDate, shiftType, today) } returns 0

            val results = mutableListOf<Collection<ShiftNotification>>()
            every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()

            service.refreshNotifications()

            assertThat(results[0]).isEqualTo(listOf<ShiftNotification>())
        }


        @Test
        fun `Should save Add shift notification types if not exist in the DB`() {
            val today = LocalDateTime.now(clock)
            val quantumId = "CSTRIFE_GEN"
            val shiftDate = today.plusDays(2).toLocalDate()
            val start = null
            val end = null
            val task = "Guard Duty"
            val shiftType = "Shift"
            val dto1 = CsrModifiedDetailDto(
                    quantumId,
                    today,
                    shiftDate,
                    shiftType,
                    start,
                    end,
                    task,
                    ShiftActionType.ADD.value
            )

            every { csrClient.getModifiedDetails(any(), any()) } returns listOf(dto1)

            every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndShiftDateAndShiftTypeIgnoreCaseAndShiftModified(quantumId, shiftDate, shiftType, today) } returns 0

            val results = mutableListOf<Collection<ShiftNotification>>()
            every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()
            service.refreshNotifications()

            val expected = ShiftNotification(null, quantumId, shiftDate, today, start, end, task, shiftType, ShiftActionType.ADD.value, false)
            assertThat(results[0]).isEqualTo(listOf(expected))
        }

        @Test
        fun `Should save remove Shift notification types if not exist in the DB`() {
            val today = LocalDateTime.now(clock)
            val quantumId = "CSTRIFE_GEN"
            val shiftDate = today.plusDays(2).toLocalDate()
            val start = null
            val end = null
            val task = "Guard Duty"
            val shiftType = "Shift"
            val dto1 = CsrModifiedDetailDto(
                    quantumId,
                    today,
                    shiftDate,
                    shiftType,
                    start,
                    end,
                    task,
                    ShiftActionType.DELETE.value
            )

            every { csrClient.getModifiedDetails(any(), any()) } returns listOf(dto1)

            every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndShiftDateAndShiftTypeIgnoreCaseAndShiftModified(quantumId, shiftDate, shiftType, today) } returns 0

            val results = mutableListOf<Collection<ShiftNotification>>()
            every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()

            service.refreshNotifications()

            val expected = ShiftNotification(null, quantumId, shiftDate, today, start, end, task, shiftType, ShiftActionType.DELETE.value, false)
            assertThat(results[0]).isEqualTo(listOf(expected))
        }

        @Test
        fun `Should disregard unprocessed Shift Notification duplicates`() {
            val today = LocalDateTime.now(clock)
            val quantumId = "CSTRIFE_GEN"
            val shiftDate = today.plusDays(2).toLocalDate()
            val start = 123L
            val end = 456L
            val task = "Guard Duty"
            val shiftType = "Shift"
            val dto1 = CsrModifiedDetailDto(
                    quantumId,
                    today,
                    shiftDate,
                    shiftType,
                    start,
                    end,
                    task,
                    ShiftActionType.ADD.value
            )

            every { csrClient.getModifiedDetails(any(), any()) } returns listOf(dto1)

            every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndShiftDateAndShiftTypeIgnoreCaseAndShiftModified(quantumId, shiftDate, shiftType, today) } returns 1

            val results = mutableListOf<Collection<ShiftNotification>>()
            every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()

            service.refreshNotifications()

            assertThat(results[0]).isEqualTo(listOf<ShiftNotification>())
        }

        @Test
        fun `Should do nothing if there is nothing to do`() {
            every { csrClient.getModifiedDetails(any(), any()) } returns listOf()

            val slot = slot<Collection<ShiftNotification>>()
            every { shiftNotificationRepository.saveAll(capture(slot)) } returns listOf()

            service.refreshNotifications()

            assertThat(slot.captured).isEqualTo(listOf<ShiftNotification>())
        }

        @Test
        fun `Should Change an Edit with no existing Add in the database to an Edit`() {
            val today = LocalDateTime.now(clock)
            val quantumId = "CSTRIFE_GEN"
            val shiftDate = today.plusDays(2).toLocalDate()
            val start = null
            val end = null
            val task = "Guard Duty"
            val shiftType = "Shift"
            val dto1 = CsrModifiedDetailDto(
                    quantumId,
                    today,
                    shiftDate,
                    shiftType,
                    start,
                    end,
                    task,
                    ShiftActionType.EDIT.value
            )

            every { csrClient.getModifiedDetails(any(), any()) } returns listOf(dto1)

            every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndShiftDateAndShiftTypeIgnoreCaseAndActionTypeIgnoreCase(quantumId, shiftDate, shiftType, ShiftActionType.ADD.value) } returns 0
            every { shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndShiftDateAndShiftTypeIgnoreCaseAndShiftModified(quantumId, shiftDate, shiftType, today) } returns 0

            val results = mutableListOf<Collection<ShiftNotification>>()
            every { shiftNotificationRepository.saveAll(capture(results)) } returns listOf()

            service.refreshNotifications()

            val expected = ShiftNotification(null, quantumId, shiftDate, today, start, end, task, shiftType, ShiftActionType.ADD.value, false)
            assertThat(results[0]).isEqualTo(listOf(expected))
        }

    }

}