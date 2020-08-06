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
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.client.CsrClient
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.NotificationDescription.Companion.getDateTimeFormattedForTemplate
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.NotificationDescription.Companion.getNotificationDescription
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.NotificationType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftActionType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.service.PrisonService
import uk.gov.service.notify.NotificationClientApi
import uk.gov.service.notify.NotificationClientException
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

@Service
@Transactional
class NotificationService(
        val shiftNotificationRepository: ShiftNotificationRepository,
        val userPreferenceService: UserPreferenceService,
        val clock: Clock,
        val authenticationFacade: AuthenticationFacade,
        @Value("\${application.to.defaultMonths}") val monthStep: Long,
        val notifyClient: NotificationClientApi,
        val prisonService: PrisonService,
        val csrClient: CsrClient
) {

    fun getNotifications(processOnReadParam: Optional<Boolean>, unprocessedOnlyParam: Optional<Boolean>, fromParam: Optional<LocalDate>, toParam: Optional<LocalDate>): Collection<NotificationDto> {
        val start = calculateStartDateTime(fromParam, toParam)
        val end = calculateEndDateTime(toParam, start)
        return getShiftNotificationDtos(start, end, unprocessedOnlyParam.orElse(false), processOnReadParam.orElse(true))
    }

    fun sendNotifications() {
        val unprocessedNotifications = shiftNotificationRepository.findAllByProcessedIsFalse()
        log.debug("Sending notifications, found: ${unprocessedNotifications.size}")

        unprocessedNotifications.groupBy { it.quantumId }
                .forEach { group ->
                    try {
                        sendNotification(group.key, group.value)
                        group.value.forEach { it.processed = true }
                    } catch (e: NotificationClientException) {
                        log.warn("Sending notifications to user ${group.key} FAILED", e)
                    }
                    log.info("Sent notification (${group.value.size} lines) for ${group.key}")
                }
        log.info("Finished sending notifications")
    }

    fun generateAndSaveNotifications() {
        val allPrisons = prisonService.getAllPrisons().distinctBy { it.csrPlanUnit }
        val newShiftNotifications = allPrisons
                .flatMap { prison ->
                    csrClient.getShiftNotifications(prison.csrPlanUnit, prison.region)
                            .filter { !checkIfNotificationsExist(it.quantumId, it.shiftDate, it.shiftType) }
                            .map {
                                it.actionType = ShiftActionType.ADD.value
                                it
                            }
                }

        val newTaskNotifications = allPrisons
                .flatMap { prison ->
                    csrClient.getShiftTaskNotifications(prison.csrPlanUnit, prison.region)
                }

        val allNotifications = newShiftNotifications.plus(newTaskNotifications)
        shiftNotificationRepository.saveAll(ShiftNotification.fromDto(allNotifications))
    }

    private fun checkIfNotificationsExist(quantumId: String, shiftDate: LocalDateTime, shiftNotificationType: String): Boolean{
        return shiftNotificationRepository.countAllByQuantumIdAndShiftDateAndShiftType(
                quantumId,
                shiftDate,
                shiftNotificationType
        ) > 0
    }

    private fun calculateStartDateTime(fromParam: Optional<LocalDate>, toParam: Optional<LocalDate>): LocalDateTime {
        val start = when {
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
        return start.atTime(LocalTime.MIN)
    }

    private fun calculateEndDateTime(toParam: Optional<LocalDate>, calculatedFromDateTime: LocalDateTime): LocalDateTime {
        val end = when {
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
        return end.atTime(LocalTime.MAX)
    }

    private fun getShiftNotificationDtos(from: LocalDateTime, to: LocalDateTime, unprocessedOnly: Boolean, processOnRead: Boolean, quantumId: String = authenticationFacade.currentUsername): Collection<NotificationDto> {
        log.debug("Finding unprocessedOnly: $unprocessedOnly notifications between $from and $to for $quantumId")
        val notifications = shiftNotificationRepository.findAllByQuantumIdAndShiftModifiedIsBetween(quantumId, from, to).filter { !unprocessedOnly || (unprocessedOnly && !it.processed) }
        log.info("Found ${notifications.size} unprocessedOnly: $unprocessedOnly notifications between $from and $to for $quantumId")
        val notificationDtos = NotificationDto.from(notifications, clock)
        if (processOnRead) {
            notifications.forEach { it.processed = true }
        }
        return notificationDtos
    }

    /*
    Group the notifications into 10s -
    Notify doesn't support vertical lists
    so we have to have a fixed size template with 'slots'
    10 means we can cover 99.9% of scenarios in one email.
    */
    private fun sendNotification(quantumId: String, notificationGroup: List<ShiftNotification>) {
        val userPreference = userPreferenceService.getOrCreateUserPreference(quantumId)

        data class Key(val shiftDate: LocalDateTime, val shiftType: String, val quantumId: String)

        fun ShiftNotification.toKeyDuplicates() = Key(this.shiftDate, this.shiftType, this.quantumId)

        val mostRecentNotifications = notificationGroup
                .groupBy { it.toKeyDuplicates() }
                .map { (key, value) -> value.maxBy { it.shiftModified } }
                .filterNotNull()

        if (shouldSend(userPreference)) {
            log.debug("Sending (${mostRecentNotifications.size}) notifications to ${userPreference.quantumId}, preference set to ${userPreference.commPref}")
            mostRecentNotifications.sortedWith(compareBy { it.shiftDate }).chunked(10).forEach { chunk ->
                when (val communicationPreference = CommunicationPreference.from(userPreference.commPref)) {
                    CommunicationPreference.EMAIL -> {
                        notifyClient.sendEmail(NotificationType.EMAIL_SUMMARY.value, userPreference.email, generateTemplateValues(chunk, communicationPreference), null)
                    }
                    CommunicationPreference.SMS -> {
                        notifyClient.sendSms(NotificationType.SMS_SUMMARY.value, userPreference.sms, generateTemplateValues(chunk, communicationPreference), null)
                    }
                    else -> {
                        log.info("Skipping sending notifications for ${userPreference.quantumId}")
                    }
                }
            }
        }
    }

    private fun shouldSend(userPreference: UserPreference): Boolean {
        val isNotSnoozed = (userPreference.snoozeUntil == null || userPreference.snoozeUntil != null && userPreference.snoozeUntil!!.isBefore(LocalDate.now(clock)))
        val isValidCommunicationMethod = when (userPreference.commPref) {
            CommunicationPreference.EMAIL.value -> {
                !userPreference.email.isNullOrBlank()
            }
            CommunicationPreference.SMS.value -> {
                !userPreference.sms.isNullOrBlank()
            }
            else -> {
                false
            }
        }
        return isNotSnoozed && isValidCommunicationMethod
    }

    private fun generateTemplateValues(chunk: List<ShiftNotification>, communicationPreference: CommunicationPreference): MutableMap<String, String?> {
        val personalisation = mutableMapOf<String, String?>()
        // Get the oldest modified date "Changes since"
        personalisation["title"] = chunk.minBy { it.shiftModified }?.shiftModified?.let { "Changes since ${getDateTimeFormattedForTemplate(it, clock)}" }
        // Map each notification onto an predefined key
        personalisation.putAll(
                notificationKeys
                        .mapIndexed { index, templateKey ->
                            templateKey to (chunk.getOrNull(index)?.let {
                                getNotificationDescription(it, communicationPreference, clock)
                            } ?: "")
                        }.toMap())
        return personalisation
    }

    companion object {

        private val log = LoggerFactory.getLogger(NotificationService::class.java)

        private val notificationKeys = listOf("not1", "not2", "not3", "not4", "not5", "not6", "not7", "not8", "not9", "not10")

    }
}