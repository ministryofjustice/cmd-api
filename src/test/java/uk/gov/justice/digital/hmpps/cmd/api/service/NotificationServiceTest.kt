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
import uk.gov.justice.digital.hmpps.cmd.api.model.UserPreference
import uk.gov.justice.digital.hmpps.cmd.api.repository.ShiftNotificationRepository
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.client.CsrClient
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.client.dto.ShiftNotificationDto
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftActionType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftNotificationType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.Prison
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.service.PrisonService
import uk.gov.service.notify.NotificationClient
import java.time.*
import java.util.*

@ExtendWith(MockKExtension::class)
@DisplayName("Notification Service tests")
internal class NotificationServiceTest {
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
    @DisplayName("Get Notification tests")
    inner class GetNotificationTests {

        @Test
        fun `Should get Notifications`() {
            val quantumId = "XYZ"
            val from = Optional.of(LocalDate.now(clock).minusDays(1))
            val to = Optional.of(LocalDate.now(clock).plusDays(1))
            val unprocessedOnly = Optional.of(false)
            val processOnRead = Optional.of(true)

            val shiftNotifications = listOf(getValidShiftNotification(clock))
            every { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getNotifications(processOnRead, unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
            confirmVerified(shiftNotificationRepository)

            assertThat(returnValue).hasSize(1)
        }

        @Test
        fun `Should get Notifications when there is only a shift notification`() {
            val quantumId = "XYZ"
            val from = Optional.of(LocalDate.now(clock).minusDays(1))
            val to = Optional.of(LocalDate.now(clock).plusDays(1))
            val unprocessedOnly = Optional.of(false)
            val processOnRead = Optional.of(true)


            val shiftNotifications = listOf(getValidShiftNotification(clock))
            every { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getNotifications(processOnRead, unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
            confirmVerified(shiftNotificationRepository)

            assertThat(returnValue).hasSize(1)
        }

        @Test
        fun `Should not get Notifications when there no notifications`() {
            val quantumId = "XYZ"
            val from = Optional.of(LocalDate.now(clock).minusDays(1))
            val to = Optional.of(LocalDate.now(clock).plusDays(1))
            val unprocessedOnly = Optional.of(false)
            val processOnRead = Optional.of(true)

            val shiftNotifications: List<ShiftNotification> = listOf()
            every { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId
            val returnValue = service.getNotifications(processOnRead, unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
            confirmVerified(shiftNotificationRepository)

            assertThat(returnValue).hasSize(0)
        }

        @Test
        fun `Should use 'from' and 'to' params`() {
            val quantumId = "XYZ"
            val from = Optional.of(LocalDate.now(clock).minusDays(1))
            val to = Optional.of(LocalDate.now(clock).plusDays(1))
            val unprocessedOnly = Optional.of(false)
            val processOnRead = Optional.of(true)

            val shiftNotifications: List<ShiftNotification> = listOf()
            every { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            service.getNotifications(processOnRead, unprocessedOnly, from, to)

            // Should use the from and to passed in.
            verify { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
            confirmVerified(shiftNotificationRepository)
        }

        @Test
        fun `Should use defaults if 'from' and 'to' params are empty`() {
            val quantumId = "XYZ"
            val from = Optional.empty<LocalDate>()
            val to = Optional.empty<LocalDate>()
            val unprocessedOnly = Optional.of(false)
            val processOnRead = Optional.of(true)

            // Should use class defaults.
            val defaultFrom = LocalDate.now(clock).withDayOfMonth(1)
            val toDate = defaultFrom.plusMonths(3)
            val defaultTo = toDate.withDayOfMonth(toDate.lengthOfMonth())

            val shiftNotifications: List<ShiftNotification> = listOf()
            every { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX)) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            service.getNotifications(processOnRead, unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX)) }
            confirmVerified(shiftNotificationRepository)
        }

        @Test
        fun `Should use default if 'from' param is empty`() {
            val quantumId = "XYZ"
            val from = Optional.empty<LocalDate>()
            val to = Optional.of(LocalDate.now(clock))
            val unprocessedOnly = Optional.of(false)
            val processOnRead = Optional.of(true)

            // Should count back 3 months to create the 'to'.
            val defaultFrom = to.get().minusMonths(3).withDayOfMonth(1)

            val shiftNotifications: List<ShiftNotification> = listOf()
            every { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            service.getNotifications(processOnRead, unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, defaultFrom.atTime(LocalTime.MIN), to.get().atTime(LocalTime.MAX)) }
            confirmVerified(shiftNotificationRepository)
        }

        @Test
        fun `Should use default if 'to' param is empty`() {
            val quantumId = "XYZ"
            val from = Optional.of(LocalDate.now(clock))
            val to = Optional.empty<LocalDate>()
            val unprocessedOnly = Optional.of(false)
            val processOnRead = Optional.of(true)

            // Should use class defaults.
            val toDate = from.get().plusMonths(3)
            val defaultTo = toDate.withDayOfMonth(toDate.lengthOfMonth())

            val shiftNotifications: List<ShiftNotification> = listOf()
            every { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX)) } returns shiftNotifications
            every { authenticationFacade.currentUsername } returns quantumId

            service.getNotifications(processOnRead, unprocessedOnly, from, to)

            verify { shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from.get().atTime(LocalTime.MIN), defaultTo.atTime(LocalTime.MAX)) }
            confirmVerified(shiftNotificationRepository)
        }
    }

    @Nested
    @DisplayName("Generate and save Notification tests")
    inner class GenerateAndSaveNotificationTests {

        @BeforeEach
        fun `set up prison fetching`() {
            val prison1 = Prison("ABC", "Main Gate", "Midgar Central", 1)
            every { prisonService.getAllPrisons() } returns listOf(prison1)
            every { csrClient.getShiftTaskNotifications(any(), any()) } returns listOf()
        }


        @Test
        fun `Should disregard edit types if it exists in our db`() {
            val today = LocalDateTime.now(clock)
            val quantumId = "CSTRIFE_GEN"
            val shiftDate = today.plusDays(2).toLocalDate()
            val start = 123L
            val end = 456L
            val task = "Guard Duty"
            val shiftType = "Shift"
            val dto1 = ShiftNotificationDto(
                    quantumId,
                    shiftDate,
                    today,
                    start,
                    end,
                    task,
                    shiftType,
                    ShiftActionType.EDIT.value
            )

            every { csrClient.getShiftNotifications(any(), any()) } returnsMany listOf(
                    listOf(dto1)
            )

            every { csrClient.getShiftTaskNotifications(any(), any()) } returns listOf()
            every { shiftNotificationRepository.countAllByQuantumIdAndShiftDateAndShiftTypeAndShiftModified(quantumId, shiftDate, shiftType, today) } returns 1

            val slot = slot<Collection<ShiftNotification>>()
            every { shiftNotificationRepository.saveAll(capture(slot)) } returns listOf()

            service.generateAndSaveNotifications()

            assertThat(slot.captured).isEqualTo(listOf<ShiftNotification>())
        }

        @Test
        fun `Should add multiple notifications for one user`() {
            val today = LocalDateTime.now(clock)
            val quantumId = "CSTRIFE_GEN"
            val start = 123L
            val end = 456L
            val task = "Guard Duty"
            val shiftType = "Shift"
            val dto1 = ShiftNotificationDto(
                    quantumId,
                    today.plusDays(1).toLocalDate(),
                    today,
                    start,
                    end,
                    task,
                    shiftType,
                    ShiftActionType.EDIT.value
            )

            val dto2 = ShiftNotificationDto(
                    quantumId,
                    today.plusDays(2).toLocalDate(),
                    today,
                    start,
                    end,
                    task,
                    shiftType,
                    ShiftActionType.EDIT.value
            )

            val dto3 = ShiftNotificationDto(
                    quantumId,
                    today.plusDays(3).toLocalDate(),
                    today,
                    start,
                    end,
                    task,
                    shiftType,
                    ShiftActionType.EDIT.value
            )

            every { csrClient.getShiftNotifications(any(), any()) } returns listOf(dto1, dto2, dto3)

            every { csrClient.getShiftTaskNotifications(any(), any()) } returns listOf()
            every { shiftNotificationRepository.countAllByQuantumIdAndShiftDateAndShiftTypeAndShiftModified(quantumId, any(), shiftType, today) } returns 0

            val slot = slot<Collection<ShiftNotification>>()
            every { shiftNotificationRepository.saveAll(capture(slot)) } returns  listOf()

            service.generateAndSaveNotifications()
            val notification1 = ShiftNotification(null, quantumId, today.plusDays(1).toLocalDate(), today, start, end, task, shiftType, ShiftActionType.EDIT.value, false)
            val notification2 = ShiftNotification(null, quantumId, today.plusDays(2).toLocalDate(), today, start, end, task, shiftType, ShiftActionType.EDIT.value, false)
            val notification3 = ShiftNotification(null, quantumId, today.plusDays(3).toLocalDate(), today, start, end, task, shiftType, ShiftActionType.EDIT.value, false)
            assertThat(slot.captured).isEqualTo(listOf(notification1, notification2, notification3))
        }

        @Test
        fun `Should not add multiple duplicate notifications for one user`() {
            val today = LocalDateTime.now(clock)
            val quantumId = "CSTRIFE_GEN"
            val start = 123L
            val end = 456L
            val task = "Guard Duty"
            val shiftType = "Shift"
            val dto1 = ShiftNotificationDto(
                    quantumId,
                    today.toLocalDate(),
                    today,
                    start,
                    end,
                    task,
                    shiftType,
                    ShiftActionType.EDIT.value
            )

            val dto2 = ShiftNotificationDto(
                    quantumId,
                    today.toLocalDate(),
                    today,
                    start,
                    end,
                    task,
                    shiftType,
                    ShiftActionType.EDIT.value
            )

            val dto3 = ShiftNotificationDto(
                    quantumId,
                    today.toLocalDate(),
                    today,
                    start,
                    end,
                    task,
                    shiftType,
                    ShiftActionType.EDIT.value
            )

            every { csrClient.getShiftNotifications(any(), any()) } returns listOf(dto1, dto2, dto3)

            every { csrClient.getShiftTaskNotifications(any(), any()) } returns listOf()
            every { shiftNotificationRepository.countAllByQuantumIdAndShiftDateAndShiftTypeAndShiftModified(quantumId, any(), shiftType, today) } returns 0

            val slot = slot<Collection<ShiftNotification>>()
            every { shiftNotificationRepository.saveAll(capture(slot)) } returns  listOf()

            service.generateAndSaveNotifications()
            val notification1 = ShiftNotification(null, quantumId, today.toLocalDate(), today, start, end, task, shiftType, ShiftActionType.EDIT.value, false)
            val notification2 = ShiftNotification(null, quantumId, today.toLocalDate(), today, start, end, task, shiftType, ShiftActionType.EDIT.value, false)
            val notification3 = ShiftNotification(null, quantumId, today.toLocalDate(), today, start, end, task, shiftType, ShiftActionType.EDIT.value, false)
            assertThat(slot.captured).isEqualTo(listOf(notification1, notification2, notification3))
        }

        @Test
        fun `Should add multiple notifications for same shift with different modified times for one user`() {
            val today = LocalDateTime.now(clock)
            val quantumId = "CSTRIFE_GEN"
            val start = 123L
            val end = 456L
            val task = "Guard Duty"
            val shiftType = "Shift"
            val dto1 = ShiftNotificationDto(
                    quantumId,
                    today.toLocalDate(),
                    today,
                    start,
                    end,
                    task,
                    shiftType,
                    ShiftActionType.EDIT.value
            )

            val dto2 = ShiftNotificationDto(
                    quantumId,
                    today.toLocalDate(),
                    today.plusSeconds(5),
                    start,
                    end,
                    task,
                    shiftType,
                    ShiftActionType.EDIT.value
            )

            val dto3 = ShiftNotificationDto(
                    quantumId,
                    today.toLocalDate(),
                    today.plusSeconds(10),
                    start,
                    end,
                    task,
                    shiftType,
                    ShiftActionType.EDIT.value
            )

            every { csrClient.getShiftNotifications(any(), any()) } returns listOf(dto1, dto2, dto3)

            every { csrClient.getShiftTaskNotifications(any(), any()) } returns listOf()
            every { shiftNotificationRepository.countAllByQuantumIdAndShiftDateAndShiftTypeAndShiftModified(quantumId, any(), shiftType, today) } returns 0
            every { shiftNotificationRepository.countAllByQuantumIdAndShiftDateAndShiftTypeAndShiftModified(quantumId, any(), shiftType, today.plusSeconds(5)) } returns 0
            every { shiftNotificationRepository.countAllByQuantumIdAndShiftDateAndShiftTypeAndShiftModified(quantumId, any(), shiftType, today.plusSeconds(10)) } returns 0

            val slot = slot<Collection<ShiftNotification>>()
            every { shiftNotificationRepository.saveAll(capture(slot)) } returns  listOf()

            service.generateAndSaveNotifications()
            val notification1 = ShiftNotification(null, quantumId, today.toLocalDate(), today, start, end, task, shiftType, ShiftActionType.EDIT.value, false)
            val notification2 = ShiftNotification(null, quantumId, today.toLocalDate(), today.plusSeconds(5), start, end, task, shiftType, ShiftActionType.EDIT.value, false)
            val notification3 = ShiftNotification(null, quantumId, today.toLocalDate(), today.plusSeconds(10), start, end, task, shiftType, ShiftActionType.EDIT.value, false)
            assertThat(slot.captured).isEqualTo(listOf(notification1,notification2,notification3))
        }

        @Test
        fun `Should add edit types if it doesn't exist in our db`() {
            val today = LocalDateTime.now(clock)
            val quantumId = "CSTRIFE_GEN"
            val shiftDate = today.plusDays(2).toLocalDate()
            val start = 123L
            val end = 456L
            val task = "Guard Duty"
            val shiftType = "Shift"
            val dto1 = ShiftNotificationDto(
                    quantumId,
                    shiftDate,
                    today,
                    start,
                    end,
                    task,
                    shiftType,
                    ShiftActionType.EDIT.value
            )

            every { csrClient.getShiftNotifications(any(), any()) } returnsMany listOf(
                    listOf(dto1)
            )
            every { shiftNotificationRepository.countAllByQuantumIdAndShiftDateAndShiftTypeAndShiftModified(quantumId, shiftDate, shiftType, today) } returns 0

            val slot = slot<Collection<ShiftNotification>>()
            every { shiftNotificationRepository.saveAll(capture(slot)) } returns listOf()

            service.generateAndSaveNotifications()

            val expected = ShiftNotification(null, quantumId, shiftDate, today, start, end, task, shiftType, ShiftActionType.EDIT.value, false)
            assertThat(slot.captured).isEqualTo(listOf(expected))
        }

        @Test
        fun `Should disregard unprocessed duplicates`() {
            val today = LocalDateTime.now(clock)
            val quantumId = "CSTRIFE_GEN"
            val shiftDate = today.plusDays(2).toLocalDate()
            val start = 123L
            val end = 456L
            val task = "Guard Duty"
            val shiftType = "Shift"
            val dto1 = ShiftNotificationDto(
                    quantumId,
                    shiftDate,
                    today,
                    start,
                    end,
                    task,
                    shiftType,
                    ShiftActionType.ADD.value
            )

            every { csrClient.getShiftNotifications(any(), any()) } returnsMany listOf(
                    listOf(dto1)
            )
            every { shiftNotificationRepository.countAllByQuantumIdAndShiftDateAndShiftTypeAndShiftModified(quantumId, shiftDate, shiftType, today) } returns 1

            val slot = slot<Collection<ShiftNotification>>()
            every { shiftNotificationRepository.saveAll(capture(slot)) } returns listOf()

            service.generateAndSaveNotifications()

            assertThat(slot.captured).isEqualTo(listOf<ShiftNotification>())
        }

        @Test
        fun `Should do nothing if there is nothing to do`() {
            every { csrClient.getShiftNotifications(any(), any()) } returns listOf()

            val slot = slot<Collection<ShiftNotification>>()
            every { shiftNotificationRepository.saveAll(capture(slot)) } returns listOf()

            service.generateAndSaveNotifications()

            assertThat(slot.captured).isEqualTo(listOf<ShiftNotification>())
        }

    }

    @Nested
    @DisplayName("Send Notification tests")
    inner class SendNotificationTests {

        @Test
        fun `Should do nothing if there are no notifications`() {
            val shiftNotifications: List<ShiftNotification> = listOf()

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
        }


        @Test
        fun `Should combine notifications to one user`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDate.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false),
                    ShiftNotification(2, quantumId1, LocalDate.now(clock).plusDays(5), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
            verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), any()) }
        }

        @Test
        fun `Should send a notification to one user`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDate.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
            verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), null) }
        }

        @Test
        fun `Should not send a notification to one user if they have a blank Email and Email Preference`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDate.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "", "sms", CommunicationPreference.EMAIL.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
        }

        @Test
        fun `Should not send a notification to one user if they have a null Email and Email Preference`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDate.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, null, "sms", CommunicationPreference.EMAIL.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
        }

        @Test
        fun `Should not send a notification to one user if they have a blank Sms and Sms Preference`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDate.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "", CommunicationPreference.SMS.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
        }

        @Test
        fun `Should not send a notification to one user if they have a null Sms and Sms Preference`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDate.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", null, CommunicationPreference.SMS.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
        }


        @Test
        fun `Should respect communication preferences Email`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDate.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
            verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), any()) }
        }

        @Test
        fun `Should respect communication preferences Sms`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDate.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.SMS.value)
            every { notifyClient.sendSms(any(), "sms", any(), any()) } returns null

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
            verify(exactly = 1) { notifyClient.sendSms(any(), "sms", any(), null) }
        }

        @Test
        fun `Should respect communication preferences None`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDate.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.NONE.value)

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
        }

        @Test
        fun `Should send notifications to two users`() {
            val quantumId1 = "XYZ"
            val quantumId2 = "ABC"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDate.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false),
                    ShiftNotification(2, quantumId2, LocalDate.now(clock).plusDays(5), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL.value)
            every { userPreferenceService.getOrCreateUserPreference(quantumId2) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId2) }
            verify(exactly = 2) { notifyClient.sendEmail(any(), "email", any(), null) }
        }

        @Test
        fun `Should send notifications to two users with different preferences`() {
            val quantumId1 = "XYZ"
            val quantumId2 = "ABC"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDate.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false),
                    ShiftNotification(2, quantumId2, LocalDate.now(clock).plusDays(5), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL.value)
            every { userPreferenceService.getOrCreateUserPreference(quantumId2) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.SMS.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null
            every { notifyClient.sendSms(any(), "sms", any(), any()) } returns null

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId2) }
            verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), null) }
            verify(exactly = 1) { notifyClient.sendSms(any(), "sms", any(), null) }
        }

        @Test
        fun `Should send notifications to two users with different preferences when the third one is 'NONE'`() {
            val quantumId1 = "XYZ"
            val quantumId2 = "ABC"
            val quantumId3 = "123"

            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDate.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false),
                    ShiftNotification(2, quantumId2, LocalDate.now(clock).plusDays(5), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false),
                    ShiftNotification(3, quantumId3, LocalDate.now(clock).plusDays(5), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.EMAIL.value)
            every { userPreferenceService.getOrCreateUserPreference(quantumId2) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.SMS.value)
            every { userPreferenceService.getOrCreateUserPreference(quantumId3) } returns UserPreference(quantumId1, null, "email", "sms", CommunicationPreference.NONE.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null
            every { notifyClient.sendSms(any(), "sms", any(), any()) } returns null


            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId2) }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId3) }
            verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), null) }
            verify(exactly = 1) { notifyClient.sendSms(any(), "sms", any(), null) }
        }

        @Test
        fun `Should only send most recent notification for duplicates'`() {
            val frozenClock = Clock.fixed(LocalDate.of(2010, 10, 29).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
            val quantumId = "CSTRIFE_GEN"
            val shiftDate = LocalDate.now(frozenClock).plusDays(4)
            val modified1 = LocalDateTime.now(frozenClock).plusHours(1)
            val modified2 = LocalDateTime.now(frozenClock).plusHours(2)
            val modified3 = LocalDateTime.now(frozenClock).plusHours(3)


            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId, shiftDate, modified1, null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false),
                    ShiftNotification(1, quantumId, shiftDate, modified2, null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false),
                    ShiftNotification(1, quantumId, shiftDate, modified3, null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false)
            )

            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId) } returns UserPreference(quantumId, null, "email", "sms", CommunicationPreference.EMAIL.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null
            every { notifyClient.sendSms(any(), "sms", any(), any()) } returns null

            val slot = slot<Map<String, String>>()
            every { notifyClient.sendEmail(any(), any(), capture(slot), null) } returns null


            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId) }
            verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), null) }
            verify(exactly = 0) { notifyClient.sendSms(any(), "sms", any(), null) }

            assertThat(slot.captured.getValue("not1")).isEqualTo("* Your shift on Tuesday, 2nd November, 2010 has been added.")
            assertThat(slot.captured.getValue("not2")).isEqualTo("")
        }

    }

    @Nested
    @DisplayName("Snooze data specific notify tests")
    inner class SendNotificationWithSnoozeTests {

        @Test
        fun `Should not send a notification if the user has a snooze preference set to future date`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDate.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false)
            )

            val snoozePref = LocalDate.now(clock).plusDays(20)
            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, snoozePref, "email", "sms", CommunicationPreference.EMAIL.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
        }

        @Test
        fun `Should not send a notification if the user has a snooze preference set to today's date`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDate.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false)
            )

            val snoozePref = LocalDate.now(clock)
            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, snoozePref, "email", "sms", CommunicationPreference.EMAIL.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }

        }

        @Test
        fun `Should send a notification if the user has a snooze preference set to yesterday's date`() {
            val quantumId1 = "XYZ"
            val shiftNotifications: List<ShiftNotification> = listOf(
                    ShiftNotification(1, quantumId1, LocalDate.now(clock).plusDays(4), LocalDateTime.now(clock), null, null, null, ShiftNotificationType.SHIFT.value, ShiftActionType.ADD.value, false)
            )

            val snoozePref = LocalDate.now(clock).minusDays(1)
            every { shiftNotificationRepository.findAllByProcessedIsFalse() } returns shiftNotifications
            every { userPreferenceService.getOrCreateUserPreference(quantumId1) } returns UserPreference(quantumId1, snoozePref, "email", "sms", CommunicationPreference.EMAIL.value)
            every { notifyClient.sendEmail(any(), "email", any(), any()) } returns null

            service.sendNotifications()

            verify { shiftNotificationRepository.findAllByProcessedIsFalse() }
            verify { userPreferenceService.getOrCreateUserPreference(quantumId1) }
            verify(exactly = 1) { notifyClient.sendEmail(any(), "email", any(), null) }

        }
    }


    companion object {
        fun getValidShiftNotification(clock: Clock): ShiftNotification {
            val date = LocalDateTime.now(clock)

            val quantumId = "XYZ"
            val shiftDate = date.plusDays(2).toLocalDate()
            val shiftModified = date.plusDays(3)
            val taskStart = 123L
            val taskEnd = 456L
            val task = "Any Activity"
            val shiftType = "shift"
            val actionType = "add"

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