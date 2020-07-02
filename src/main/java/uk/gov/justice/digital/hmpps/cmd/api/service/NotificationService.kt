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
import java.time.LocalTime
import java.util.*

@Service
class NotificationService(@Autowired val shiftNotificationRepository: ShiftNotificationRepository, @Autowired val shiftTaskNotificationRepository: ShiftTaskNotificationRepository, @Autowired val clock: Clock, @Autowired val authenticationFacade: AuthenticationFacade, @Value("\${notifications.to.defaultMonths}") var plusMonths: Long) {

    @Transactional
    fun getNotifications(unreadOnlyParam: Optional<Boolean>, fromParam: Optional<LocalDate>, toParam: Optional<LocalDate>): Collection<NotificationDto> {
        val quantumId = authenticationFacade.currentUsername

        val unreadOnly = unreadOnlyParam.orElse(false)

        val now = LocalDate.now(clock)
        val from = fromParam.orElse(now.withDayOfMonth(1)).atTime(LocalTime.MIN)
        val to = toParam.orElse(now.plusMonths(plusMonths).withDayOfMonth(now.lengthOfMonth())).atTime(LocalTime.MAX)
        log.debug("Finding UnreadOnly: ($unreadOnly) Notifications between ($from) and ($to)")

        val shifts = shiftNotificationRepository.findAllByQuantumIdAndLastModifiedDateTimeIsBetween(quantumId, from, to)
        val details = shiftTaskNotificationRepository.findAllByQuantumIdAndLastModifiedDateTimeIsBetween(quantumId, from, to)
        log.debug("Found (${shifts.size}) Shift and (${details.size}) Detail Notifications")

        val shiftDtos = NotificationDto.fromShift(shifts.filter { !unreadOnly || (unreadOnly && !it.read) })
        val detailDtos = NotificationDto.fromTask(details.filter { !unreadOnly || (unreadOnly && !it.read) })

        shifts.forEach { it.read = true }
        details.forEach { it.read = true }

        shiftNotificationRepository.saveAll(shifts)
        shiftTaskNotificationRepository.saveAll(details)
        log.debug("Updated (${shifts.size}) Shift and (${details.size}) Detail Notifications")

        return shiftDtos.union(detailDtos)
    }

    companion object {
        private val log = LoggerFactory.getLogger(NotificationService::class.java)
    }
}