package uk.gov.justice.digital.hmpps.cmd.api.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.cmd.api.dto.NotificationDto
import uk.gov.justice.digital.hmpps.cmd.api.model.ShiftNotification
import uk.gov.justice.digital.hmpps.cmd.api.model.UserPreference
import uk.gov.justice.digital.hmpps.cmd.api.repository.ShiftNotificationRepository
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.CommunicationPreference
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.ShiftActionType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.ShiftNotificationType
import uk.gov.service.notify.NotificationClientApi
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class NotificationService(val shiftNotificationRepository: ShiftNotificationRepository, val userPreferenceService: UserPreferenceService, val clock: Clock, val authenticationFacade: AuthenticationFacade, @Value("\${application.to.defaultMonths}") val monthStep: Long, val notificationClient: NotificationClientApi) {

    @Transactional
    fun getNotifications(unprocessedOnlyParam: Optional<Boolean>, fromParam: Optional<LocalDate>, toParam: Optional<LocalDate>): Collection<NotificationDto> {
        val quantumId = authenticationFacade.currentUsername

        val unprocessedOnly = unprocessedOnlyParam.orElse(false)
        val from = calculateFromDateTime(fromParam, toParam)
        val to = calculateToDateTime(toParam, from)
        log.debug("Finding unprocessedOnly: ($unprocessedOnly) Notifications between ($from) and ($to)")

        val notificationDtos = getShiftNotifications(quantumId, from, to, unprocessedOnly)
        log.debug("Found (${notificationDtos.size}) Shift Notifications")
        return notificationDtos
    }

    fun sendNotifications() {
        // Get the notifications from the database
        val allNotifications = shiftNotificationRepository.findAllByProcessedIsFalse()
        // Group the notifications by quantumId and for each group
        allNotifications.groupBy { it.quantumId }.forEach { notificationGroup ->
            // Get the communication preferences for that quantumId
            val userPreference = userPreferenceService.getUserPreference(notificationGroup.key)
            val userNotifications = notificationGroup.value
            if (userPreference != null) {
                // If the user wants to receive notifications
                if (userPreference.commPref != CommunicationPreference.NONE.value) {
                    log.info("Sending notifications for ${userPreference.quantumId}, preference set to ${userPreference.commPref}")
                    sendNotificationSummary(userNotifications, userPreference)
                } else {
                    log.info("Skipping sending notifications for ${userPreference.quantumId}, preference set to ${userPreference.commPref}")
                }
                userNotifications.forEach { it.processed = true }
                shiftNotificationRepository.saveAll(userNotifications)
            }
        }
    }

    /*
    Group the notifications into 10s -
    Notify doesn't support vertical lists
    so we have to have a fixed size template with 'slots'
    10 means we can cover 99.9% of scenarios in one email.
    */
    private fun sendNotificationSummary(notificationGroup: List<ShiftNotification>, userPreference: UserPreference) {
        notificationGroup.chunked(10).forEach { notificationChunk ->

            // Get the oldest modified date "notifications changed since..."
            val titleDate = notificationChunk.minBy { it.shiftModified }?.shiftModified.toString()

            // Map each notification onto an predefined key
            val notificationDescriptions = notificationKeys.mapIndexed { index, s ->
                s to if (notificationChunk.size < index) getNotificationDescription(notificationChunk[index]) else ""
            }.toMap()

            val personalisation = mutableMapOf<String, String>()
            personalisation["titleDate"] = titleDate
            personalisation.putAll(notificationDescriptions)

            when (userPreference.commPref) {
                CommunicationPreference.EMAIL.value -> {
                    notificationClient.sendEmail("whatevertemplate", userPreference.email, personalisation, null)
                }
                CommunicationPreference.SMS.value -> {
                    notificationClient.sendSms("whatevertemplate", userPreference.sms, personalisation, null)
                }
            }
        }
    }

    private fun getShiftNotifications(quantumId: String, fromDateTime: LocalDateTime, toDateTime: LocalDateTime, unprocessedOnly: Boolean): List<NotificationDto> {
        val notifications = shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(
                quantumId,
                fromDateTime,
                toDateTime).filter { filterUnread(unprocessedOnly, it.processed) }
        val notificationDtos = notifications.map { NotificationDto.from(it, getNotificationDescription(it)) }
        notifications.forEach { it.processed = true }
        shiftNotificationRepository.saveAll(notifications)
        return notificationDtos
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

    companion object {

        fun getNotificationDescription(shiftNotification: ShiftNotification): String {
            val type = ShiftNotificationType.valueOf(shiftNotification.shiftType).value
            val action = ShiftActionType.valueOf(shiftNotification.actionType).value
            return "Your $type on ${dateFormat.format(shiftNotification.shiftDate)} has $action"
        }

        private val log = LoggerFactory.getLogger(NotificationService::class.java)

        private val dateFormat = DateTimeFormatter.ofPattern("EEEE, MMMM d")

        private val notificationKeys: List<String> = listOf("not1", "not2", "not3", "not4", "not5", "not6", "not8", "not9", "not10")

        private fun filterUnread(unprocessedOnly: Boolean, read: Boolean) = !unprocessedOnly || (unprocessedOnly && !read)

    }
}