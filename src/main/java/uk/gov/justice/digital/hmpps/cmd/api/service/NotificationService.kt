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
class NotificationService(@Autowired val shiftNotificationRepository: ShiftNotificationRepository, @Autowired val shiftTaskNotificationRepository: ShiftTaskNotificationRepository, @Autowired val clock: Clock, @Autowired val authenticationFacade: AuthenticationFacade, @Value("\${notifications.to.defaultMonths}") var plusMonths: Long) {

    @Transactional
    fun getNotifications(unreadOnlyParam: Optional<Boolean>, fromParam: Optional<LocalDate>, toParam: Optional<LocalDate>): Collection<NotificationDto> {
        val quantumId = authenticationFacade.currentUsername

        val unreadOnly = unreadOnlyParam.orElse(false)
        val (fromDateTime, toDateTime) = calculateFromAndToDates(fromParam, toParam)
        log.debug("Finding UnreadOnly: ($unreadOnly) Notifications between ($fromDateTime) and ($toDateTime)")

        val shiftDtos = getShiftNotifications(quantumId, fromDateTime, toDateTime, unreadOnly)
        val taskDtos = getShiftTaskNotifications(quantumId, fromDateTime, toDateTime, unreadOnly)
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

    private fun calculateFromAndToDates(fromParam: Optional<LocalDate>, toParam: Optional<LocalDate>): Pair<LocalDateTime, LocalDateTime> {
        val now = LocalDate.now(clock)

        // Default 'from' date is day 1 of the current month
        val from = fromParam.orElse(now.withDayOfMonth(1))
        val fromDateTime = from.atTime(LocalTime.MIN)

        // Default 'to' date is the last day of the month 3 month's after the to date
        val toDate = from.plusMonths(plusMonths)
        val to = toParam.orElse(toDate.withDayOfMonth(toDate.lengthOfMonth()))
        val toDateTime = to.atTime(LocalTime.MAX)

        return Pair(fromDateTime, toDateTime)
    }

    private fun filterUnread(unreadOnly: Boolean, read: Boolean) =
            !unreadOnly || (unreadOnly && !read)

    companion object {
        private val log = LoggerFactory.getLogger(NotificationService::class.java)
    }
}