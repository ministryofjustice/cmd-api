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
    fun getNotifications(unprocessedOnlyParam: Optional<Boolean>, fromParam: Optional<LocalDate>, toParam: Optional<LocalDate>): Collection<NotificationDto> {
        val quantumId = authenticationFacade.currentUsername

        val unprocessedOnly = unprocessedOnlyParam.orElse(false)
        val from = calculateFromDateTime(fromParam, toParam)
        val to = calculateToDateTime(toParam, from)
        log.debug("Finding unprocessedOnly: ($unprocessedOnly) Notifications between ($from) and ($to)")

        val shiftDtos = getShiftNotifications(quantumId, from, to, unprocessedOnly)
        val taskDtos = getShiftTaskNotifications(quantumId, from, to, unprocessedOnly)
        log.debug("Found (${shiftDtos.size}) Shift and (${taskDtos.size}) Task Notifications")
        return shiftDtos.union(taskDtos)
    }

    private fun getShiftTaskNotifications(quantumId: String, fromDateTime: LocalDateTime, toDateTime: LocalDateTime, unprocessedOnly: Boolean): List<NotificationDto> {
        val tasks = shiftTaskNotificationRepository.findAllByQuantumIdAndLastModifiedDateTimeIsBetween(
                quantumId,
                fromDateTime,
                toDateTime).filter { filterUnread(unprocessedOnly, it.processed) }
        val taskDtos = NotificationDto.fromTasks(tasks)
        tasks.forEach { it.processed = true }
        shiftTaskNotificationRepository.saveAll(tasks)
        return taskDtos
    }

    private fun getShiftNotifications(quantumId: String, fromDateTime: LocalDateTime, toDateTime: LocalDateTime, unprocessedOnly: Boolean): List<NotificationDto> {
        val shifts = shiftNotificationRepository.findAllByQuantumIdAndLastModifiedDateTimeIsBetween(
                quantumId,
                fromDateTime,
                toDateTime).filter { filterUnread(unprocessedOnly, it.processed) }
        val shiftDtos = NotificationDto.fromShifts(shifts)
        shifts.forEach { it.processed = true }
        shiftNotificationRepository.saveAll(shifts)
        return shiftDtos
    }

    private fun calculateFromDateTime(fromParam: Optional<LocalDate>, toParam: Optional<LocalDate>): LocalDateTime {
        val from = when {
            fromParam.isPresent -> {
                // Use the passed in 'from' param
                fromParam.get()
            }
            toParam.isPresent -> {
                // Set the 'from' to be the start day of 3 months into the relative past
                toParam.get().minusMonths(monthStep).withDayOfMonth(1)
            }
            else -> {
                // Use the default
                LocalDate.now(clock).withDayOfMonth(1)
            }
        }
        return from.atTime(LocalTime.MIN)
    }

    private fun calculateToDateTime(toParam: Optional<LocalDate>, calculatedFromDateTime: LocalDateTime): LocalDateTime {
        val to = when {
            toParam.isPresent -> {
                // Use the passed in 'from' param
                toParam.get()
            }
            else -> {
                // Use the default
                val toDate = calculatedFromDateTime.toLocalDate().plusMonths(monthStep)
                toDate.withDayOfMonth(toDate.lengthOfMonth())
            }
        }
        return to.atTime(LocalTime.MAX)
    }

    private fun filterUnread(unprocessedOnly: Boolean, read: Boolean) =
            !unprocessedOnly || (unprocessedOnly && !read)

    companion object {
        private val log = LoggerFactory.getLogger(NotificationService::class.java)
    }
}