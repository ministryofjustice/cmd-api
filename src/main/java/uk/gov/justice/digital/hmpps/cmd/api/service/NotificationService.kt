package uk.gov.justice.digital.hmpps.cmd.api.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.cmd.api.dto.NotificationDto
import uk.gov.justice.digital.hmpps.cmd.api.repository.ShiftNotificationRepository
import uk.gov.justice.digital.hmpps.cmd.api.repository.ShiftTaskNotificationRepository
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

@Service
class NotificationService(@Autowired val shiftNotificationRepository: ShiftNotificationRepository, @Autowired val shiftTaskNotificationRepository: ShiftTaskNotificationRepository, @Autowired val clock: Clock, @Autowired val authenticationFacade: AuthenticationFacade, @Value("\${notifications.to.defaultMonths}") var monthStep: Long) {

    @Transactional
    fun getNotifications(unreadOnlyParam: Optional<Boolean>, fromParam: Optional<LocalDate>, toParam: Optional<LocalDate>): Collection<NotificationDto> {
        val quantumId = authenticationFacade.currentUsername

        val unreadOnly = unreadOnlyParam.orElse(false)
        val from = calculateFromDateTime(fromParam, toParam)
        val to = calculateToDateTime(toParam, from)
        log.debug("Finding UnreadOnly: ($unreadOnly) Notifications between ($from) and ($to)")

        val shiftDtos = getShiftNotifications(quantumId, from, to, unreadOnly)
        val taskDtos = getShiftTaskNotifications(quantumId, from, to, unreadOnly)
        log.debug("Found (${shiftDtos.size}) Shift and (${taskDtos.size}) Task Notifications")
        return shiftDtos.union(taskDtos)
    }

    private fun getShiftTaskNotifications(quantumId: String, fromDateTime: LocalDateTime, toDateTime: LocalDateTime, unreadOnly: Boolean): List<NotificationDto> {
        val tasks = shiftTaskNotificationRepository.findAllByQuantumIdAndLastModifiedDateTimeIsBetween(
                quantumId,
                fromDateTime,
                toDateTime).filter { filterUnread(unreadOnly, it.read) }
        val taskDtos = NotificationDto.fromTasks(tasks)
        tasks.forEach { it.read = true }
        shiftTaskNotificationRepository.saveAll(tasks)
        return taskDtos
    }

    private fun getShiftNotifications(quantumId: String, fromDateTime: LocalDateTime, toDateTime: LocalDateTime, unreadOnly: Boolean): List<NotificationDto> {
        val shifts = shiftNotificationRepository.findAllByQuantumIdAndLastModifiedDateTimeIsBetween(
                quantumId,
                fromDateTime,
                toDateTime).filter { filterUnread(unreadOnly, it.read) }
        val shiftDtos = NotificationDto.fromShifts(shifts)
        shifts.forEach { it.read = true }
        shiftNotificationRepository.saveAll(shifts)
        return shiftDtos
    }

    private fun calculateFromDateTime(fromParam: Optional<LocalDate>, toParam: Optional<LocalDate>): LocalDateTime {
        val from = if (fromParam.isPresent) {
            // Use the passed in 'from' param
            fromParam.get()
        } else if (toParam.isPresent) {
            // Set the 'from' to be the start day of 3 months into the relative past
            toParam.get().minusMonths(monthStep).withDayOfMonth(1)
        } else {
            // Use the default
            LocalDate.now(clock).withDayOfMonth(1)
        }
        return from.atTime(LocalTime.MIN)
    }

    private fun calculateToDateTime(toParam: Optional<LocalDate>, calculatedFromDateTime: LocalDateTime): LocalDateTime {
        val to = if (toParam.isPresent) {
            // Use the passed in 'from' param
            toParam.get()
        } else {
            // Use the default
            val toDate = calculatedFromDateTime.toLocalDate().plusMonths(monthStep)
            toDate.withDayOfMonth(toDate.lengthOfMonth())
        }
        return to.atTime(LocalTime.MAX)
    }

    private fun filterUnread(unreadOnly: Boolean, read: Boolean) =
            !unreadOnly || (unreadOnly && !read)

    companion object {
        private val log = LoggerFactory.getLogger(NotificationService::class.java)
    }
}