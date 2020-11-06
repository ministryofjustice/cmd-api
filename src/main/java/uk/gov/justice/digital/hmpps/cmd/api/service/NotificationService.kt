package uk.gov.justice.digital.hmpps.cmd.api.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrClient
import uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailModificationType
import uk.gov.justice.digital.hmpps.cmd.api.domain.NotificationType
import uk.gov.justice.digital.hmpps.cmd.api.dto.NotificationDto
import uk.gov.justice.digital.hmpps.cmd.api.model.Notification
import uk.gov.justice.digital.hmpps.cmd.api.model.Notification.Companion.getDateTimeFormattedForTemplate
import uk.gov.justice.digital.hmpps.cmd.api.model.UserPreference
import uk.gov.justice.digital.hmpps.cmd.api.repository.NotificationRepository
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.DetailParentType
import uk.gov.service.notify.NotificationClientApi
import uk.gov.service.notify.NotificationClientException
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

@Service
class NotificationService(
        val shiftNotificationRepository: NotificationRepository,
        val userPreferenceService: UserPreferenceService,
        val clock: Clock,
        val authenticationFacade: AuthenticationFacade,
        @Value("\${application.to.defaultMonths}") val monthStep: Long,
        val notifyClient: NotificationClientApi,
        val prisonService: PrisonService,
        val csrClient: CsrClient) {

    fun getNotifications(processOnReadParam: Optional<Boolean>, unprocessedOnlyParam: Optional<Boolean>, fromParam: Optional<LocalDate>, toParam: Optional<LocalDate>, quantumId: String = authenticationFacade.currentUsername): Collection<NotificationDto> {
        val from = calculateStartDateTime(fromParam, toParam)
        val to = calculateEndDateTime(toParam, from)
        val unprocessedOnly = unprocessedOnlyParam.orElse(false)
        val processOnRead = processOnReadParam.orElse(true)
        log.debug("User Notifications: finding user $quantumId, unprocessedOnly: $unprocessedOnly")
        val notifications = getNotifications(quantumId, from, to, unprocessedOnly)
        log.info("User Notifications: found ${notifications.size} user $quantumId, unprocessedOnly: $unprocessedOnly")

        val notificationDtos = notifications.map {
            NotificationDto.from(it, CommunicationPreference.NONE)
        }

        if (processOnRead) {
            notifications.forEach { it.processed = true }
            shiftNotificationRepository.saveAll(notifications)
        }
        return notificationDtos.distinct()
    }

    fun sendNotifications() {
        val unprocessedNotifications = shiftNotificationRepository.findAllByProcessedIsFalse()
        log.info("Sending notifications, found: ${unprocessedNotifications.size}")
        unprocessedNotifications.groupBy { it.quantumId }
                .forEach { group ->
                    try {
                        sendNotification(group.key, group.value)
                        group.value.forEach { it.processed = true }
                        shiftNotificationRepository.saveAll(group.value)
                    } catch (e: NotificationClientException) {
                        log.warn("Sending notifications to user ${group.key} FAILED", e)
                    }
                    log.info("Sent notification (${group.value.size} lines) for ${group.key}")
                }
        log.info("Finished sending notifications")
    }

    fun refreshNotifications(region : Int) {
        log.info("Refreshing modified details for region: $region")
        val allPrisons = prisonService.getAllPrisons().filter{ it.region == region }.distinctBy { it.csrPlanUnit }
        val newNotifications = allPrisons
                .flatMap { prison ->
                    csrClient.getModifiedDetails(prison.csrPlanUnit, prison.region)
                }.distinct()
                .map{
                    // We only want to transform shift level changes, not detail changes.
                    if(it.activity == null && it.actionType == DetailModificationType.EDIT && !checkIfEditNotificationsHasCorrespondingAdd(it.quantumId, it.detailStart, it.shiftType)) {
                       it.actionType = DetailModificationType.ADD
                    }
                    it
                }

        val allNotifications = newNotifications
                // We want to remove Shift level changes that aren't 'add'
                // we want to filter anything that is unchanged
                .filterNot { (it.activity == null && it.actionType == DetailModificationType.EDIT) ||
                        it.actionType == DetailModificationType.UNCHANGED ||
                        checkIfNotificationsExist(it.quantumId, it.detailStart, it.shiftType, it.shiftModified)
                }
        shiftNotificationRepository.saveAll(Notification.fromDto(allNotifications))
        log.info("Completed Refreshing modified details for region: $region")
    }

    fun tidyNotification() {
        // Only hold on to 3 months of this temporary data.
        var startOfDay = LocalDate.now(clock).atStartOfDay().minusMonths(3)
        log.info("Removing old notifications (before $startOfDay)")
        shiftNotificationRepository.deleteAllByShiftModifiedBefore(startOfDay)
        log.info("Removed old notifications (before $startOfDay)")
    }

    private fun getNotifications(quantumId: String, start: LocalDateTime, end: LocalDateTime, unprocessedOnly: Boolean): Collection<Notification> {
        return shiftNotificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(
                quantumId,
                start,
                end)
                .filter { !unprocessedOnly || (unprocessedOnly && !it.processed) }
    }

    private fun checkIfNotificationsExist(quantumId: String, detailStart: LocalDateTime, shiftType: DetailParentType, shiftModified: LocalDateTime): Boolean {
        return shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
                quantumId,
                detailStart,
                shiftType,
                shiftModified) > 0
    }

    private fun checkIfEditNotificationsHasCorrespondingAdd(quantumId: String, detailStart: LocalDateTime, shiftType: DetailParentType): Boolean{
        return shiftNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndActionType(
                quantumId,
                detailStart,
                shiftType,
                DetailModificationType.ADD) > 0
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

    /*
    * Chunk the notifications into 10s -
    * Notify doesn't support vertical lists
    * so we have to have a fixed size template with 'slots'
    * 10 means we can cover 99.9% of scenarios in one email.
    */
    private fun sendNotification(quantumId: String, notificationGroup: List<Notification>) {
        val userPreference = userPreferenceService.getOrCreateUserPreference(quantumId)

        data class Key(val shiftType: DetailParentType, val quantumId: String, val detailStart: LocalDateTime)

        fun Notification.toKeyDuplicates() = Key(this.parentType, this.quantumId.toUpperCase(), this.detailStart)

        // Only send the latest notification for a shift if there are multiple
        val mostRecentNotifications = notificationGroup
                .groupBy { it.toKeyDuplicates() }
                .map { (_, value) -> value.maxByOrNull { it.shiftModified } }
                .filterNotNull()

        if (shouldSend(userPreference)) {
            log.debug("Sending (${mostRecentNotifications.size}) notifications to ${userPreference.quantumId}, preference set to ${userPreference.commPref}")
            mostRecentNotifications.sortedWith(compareBy { it.detailStart }).chunked(10).forEach { chunk ->
                when (val communicationPreference = userPreference.commPref) {
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
            CommunicationPreference.EMAIL -> {
                !userPreference.email.isNullOrBlank()
            }
            CommunicationPreference.SMS -> {
                !userPreference.sms.isNullOrBlank()
            }
            else -> {
                false
            }
        }
        return isNotSnoozed && isValidCommunicationMethod
    }

    private fun generateTemplateValues(chunk: List<Notification>, communicationPreference: CommunicationPreference): MutableMap<String, String?> {
        val personalisation = mutableMapOf<String, String?>()
        // Get the oldest modified date "Changes since"
        personalisation["title"] = chunk.minByOrNull { it.shiftModified }?.shiftModified?.let { "Changes since ${it.getDateTimeFormattedForTemplate()}" }
        // Map each notification onto an predefined key
        personalisation.putAll(
                notificationKeys
                        .mapIndexed { index, templateKey ->
                            templateKey to (chunk.getOrNull(index)?.getNotificationDescription(communicationPreference)
                                    ?: "")
                        }.toMap())
        return personalisation
    }

    companion object {

        private val log = LoggerFactory.getLogger(NotificationService::class.java)

        private val notificationKeys = listOf("not1", "not2", "not3", "not4", "not5", "not6", "not7", "not8", "not9", "not10")

    }
}