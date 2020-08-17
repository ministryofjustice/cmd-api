package uk.gov.justice.digital.hmpps.cmd.api.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.cmd.api.dto.NotificationDto
import uk.gov.justice.digital.hmpps.cmd.api.model.ShiftNotification
import uk.gov.justice.digital.hmpps.cmd.api.model.ShiftNotification.Companion.getDateTimeFormattedForTemplate
import uk.gov.justice.digital.hmpps.cmd.api.repository.ShiftNotificationRepository
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.client.CsrClient
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.client.dto.ShiftNotificationDto
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference
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
import javax.transaction.Transactional

@Service
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

    @Transactional
    fun getNotifications(processOnReadParam: Optional<Boolean>, unprocessedOnlyParam: Optional<Boolean>, fromParam: Optional<LocalDate>, toParam: Optional<LocalDate>): Collection<NotificationDto> {
        val start = calculateStartDateTime(fromParam, toParam)
        val end = calculateEndDateTime(toParam, start)
        return getShiftNotificationDtos(start, end, unprocessedOnlyParam.orElse(false), processOnReadParam.orElse(true))
    }

    @Transactional
    fun sendNotifications() {
        log.info("Sending Notifications")
        shiftNotificationRepository.findAllByProcessedIsFalse()
                .groupBy { it.quantumId }
                .forEach { group ->
                    try{
                        sendNotification(group.key, group.value)
                        group.value.forEach { it.processed = true }
                    } catch (e: NotificationClientException) {
                        log.warn("Sending notifications to user ${group.key} FAILED", e)
                    }
                }
        log.info("Finished sending notifications")
    }

    fun refreshNotifications() {
        log.info("Refreshing notifications")
        val allPrisons = prisonService.getAllPrisons().distinctBy { it.csrPlanUnit }
        log.info("Found ${allPrisons.size} unique planUnits")

        allPrisons.forEach { prison ->
            saveNotifications(
                    csrClient.getShiftTaskNotifications(prison.csrPlanUnit, prison.region)
                            .distinct())
            saveNotifications(
                    csrClient.getShiftNotifications(prison.csrPlanUnit, prison.region)
                            .distinct()
                            .map{
                                if(ShiftActionType.from(it.actionType) == ShiftActionType.EDIT &&
                                        !checkIfNotificationHasCorrespondingAdd(it.quantumId, it.actionDate, it.shiftType)) {
                                    // Some manually created shifts start off with an action type of Edit
                                    // Identify them and change them to Add.
                                    it.actionType = ShiftActionType.ADD.value
                                }
                                it
                            }
                            // Shift notifications cover Add and Delete, Task notifications cover Edit
                            // as they have more detail for Edits.
                            .filter { ShiftActionType.from(it.actionType) != ShiftActionType.EDIT })

        }
    }

    private fun saveNotifications(notifications: Collection<ShiftNotificationDto>) {
        val filteredNotifications = notifications
                .filter { ShiftActionType.from(it.actionType) != ShiftActionType.UNCHANGED }
                .filter { !checkIfNotificationsExist(it.quantumId, it.actionDate, it.shiftType, it.shiftModified) }
        shiftNotificationRepository.saveAll(ShiftNotification.fromDto(filteredNotifications))
    }

    private fun getNotifications(quantumId: String, start: LocalDateTime, end: LocalDateTime, unprocessedOnly: Boolean) : Collection<ShiftNotification> {
        return shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(
                quantumId,
                start,
                end)
                .filter { !unprocessedOnly || (unprocessedOnly && !it.processed) }
    }

    private fun checkIfNotificationsExist(quantumId: String, shiftDate: LocalDate, shiftNotificationType: String, shiftModified : LocalDateTime): Boolean{
        return shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndShiftDateAndShiftTypeIgnoreCaseAndShiftModified(
                quantumId,
                shiftDate,
                shiftNotificationType,
                shiftModified) > 0
    }

    private fun checkIfNotificationHasCorrespondingAdd(quantumId: String, shiftDate: LocalDate, shiftNotificationType: String): Boolean{
        return shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndShiftDateAndShiftTypeIgnoreCaseAndActionTypeIgnoreCase(
                quantumId,
                shiftDate,
                shiftNotificationType,
                ShiftActionType.ADD.value) > 0
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
        val notifications = getNotifications(quantumId, from, to, unprocessedOnly)
        log.info("Found ${notifications.size} unprocessedOnly: $unprocessedOnly notifications between $from and $to for $quantumId")

        val notificationDtos = notifications.map {
            NotificationDto.from(it, CommunicationPreference.NONE)
        }

        if (processOnRead) {
            notifications.forEach { it.processed = true }
        }
        return notificationDtos.distinct()
    }

    /*
    * Chunk the notifications into 10s -
    * Notify doesn't support vertical lists
    * so we have to have a fixed size template with 'slots'
    * 10 means we can cover 99.9% of scenarios in one email.
    */
    private fun sendNotification(quantumId: String, notificationGroup: List<ShiftNotification>) {
        log.info("Found ${notificationGroup.size} results for $quantumId")

        val userPreference = userPreferenceService.getOrCreateUserPreference(quantumId)

        if (!userHasSnoozedNotifications(userPreference.snoozeUntil)) {
            // Only send the latest notification for a shift if there are multiple
            notificationGroup
                    .asSequence()
                    .groupBy { it.compoundKey() }
                    .map { (_, value) -> value.maxBy { it.shiftModified } }.filterNotNull()
                    .sortedBy { it.shiftDate }.chunked(10)
                    .forEach { chunk ->
                        log.info("Sending ${chunk.size} deduplicated notifications to ${userPreference.quantumId}, preference set to ${userPreference.commPref}")

                        when (val communicationPreference = CommunicationPreference.from(userPreference.commPref)) {
                            CommunicationPreference.EMAIL -> {
                                notifyClient.sendEmail(NotificationType.EMAIL_SUMMARY.value, userPreference.email, generateTemplateValues(chunk, communicationPreference), null)
                                log.info("Sent Email notification (${chunk}.size} lines) for $quantumId")
                            }
                            CommunicationPreference.SMS -> {
                                notifyClient.sendSms(NotificationType.SMS_SUMMARY.value, userPreference.sms, generateTemplateValues(chunk, communicationPreference), null)
                                log.info("Sent SMS notification (${chunk}.size} lines) for $quantumId")
                            }
                            else -> {
                                log.info("Skipped sending notifications for ${userPreference.quantumId}, preference ${userPreference.commPref}")
                            }
                        }
                    }
        } else {
            log.info("Skipped sending notifications to ${userPreference.quantumId}, snooze set to ${userPreference.snoozeUntil}")
        }
    }

    private fun userHasSnoozedNotifications(snoozeUntil: LocalDate?): Boolean {
        return when (snoozeUntil) {
            null -> {
                false
            }
            else -> {
                !snoozeUntil.isBefore(LocalDate.now(clock))
            }
        }
    }

    private fun generateTemplateValues(chunk: List<ShiftNotification>, communicationPreference: CommunicationPreference): MutableMap<String, String?> {
        val personalisation = mutableMapOf<String, String?>()
        // Get the oldest modified date "Changes since"
        personalisation["title"] = chunk.minBy { it.shiftModified }?.shiftModified?.let { "Changes since ${(it.toLocalDate().getDateTimeFormattedForTemplate())}" }
        // Map each notification onto an predefined key
        personalisation.putAll(
                notificationKeys
                        .mapIndexed { index, templateKey ->
                            templateKey to (chunk.getOrNull(index)?.getNotificationDescription(communicationPreference) ?: "")
                        }.toMap())
        return personalisation
    }

    companion object {

        data class Key(val shiftDate: LocalDate, val shiftType: String, val quantumId: String)
        fun ShiftNotification.compoundKey() = Key(this.shiftDate, this.shiftType.toUpperCase(), this.quantumId.toUpperCase())

        private val log = LoggerFactory.getLogger(NotificationService::class.java)

        private val notificationKeys = listOf("not1", "not2", "not3", "not4", "not5", "not6", "not7", "not8", "not9", "not10")

    }
}