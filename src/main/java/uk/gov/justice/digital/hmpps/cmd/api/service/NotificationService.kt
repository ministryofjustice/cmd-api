package uk.gov.justice.digital.hmpps.cmd.api.service

import com.microsoft.applicationinsights.TelemetryClient
import org.apache.commons.lang3.time.DurationFormatUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrClient
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrModifiedDetailDto
import uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailModificationType
import uk.gov.justice.digital.hmpps.cmd.api.domain.NotificationType
import uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.cmd.api.dto.NotificationDto
import uk.gov.justice.digital.hmpps.cmd.api.model.Notification
import uk.gov.justice.digital.hmpps.cmd.api.model.Notification.Companion.getDateTimeFormattedForTemplate
import uk.gov.justice.digital.hmpps.cmd.api.model.UserPreference
import uk.gov.justice.digital.hmpps.cmd.api.repository.NotificationRepository
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import uk.gov.service.notify.NotificationClientApi
import uk.gov.service.notify.NotificationClientException
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.Optional

private const val CUTOFF_MINUTES = 5L

@Service
@Transactional // all public methods do updates, including getNotifications() !
class NotificationService(
  private val notificationRepository: NotificationRepository,
  private val userPreferenceService: UserPreferenceService,
  private val clock: Clock,
  private val authenticationFacade: AuthenticationFacade,
  @Value("\${application.to.defaultMonths}")
  private val monthStep: Long,
  private val notifyClient: NotificationClientApi,
  private val csrClient: CsrClient,
  private val telemetryClient: TelemetryClient,
) {

  fun getNotifications(
    processOnReadParam: Optional<Boolean>,
    unprocessedOnlyParam: Optional<Boolean>,
    fromParam: Optional<LocalDate>,
    toParam: Optional<LocalDate>,
  ): Collection<NotificationDto> {
    val quantumId = authenticationFacade.currentUsername
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
      notificationRepository.saveAll(notifications)
    }
    return notificationDtos.distinct()
  }

  @Transactional(propagation = Propagation.NEVER)
  fun sendNotifications() {
    val unprocessedNotifications = notificationRepository.findAllByProcessedIsFalse()
    log.info("sendNotifications: Sending notifications, found: ${unprocessedNotifications.size}")
    unprocessedNotifications.groupBy { it.quantumId }
      .forEach { group ->
        try {
          sendNotification(group.key, group.value)
          group.value.forEach { it.processed = true }
          notificationRepository.saveAll(group.value)
          log.info("sendNotifications: Sent notification (${group.value.size} lines) for ${group.key}")
        } catch (e: NotificationClientException) {
          log.warn("sendNotifications: Sending notifications to user ${group.key} ${group.value} FAILED", e)
        }
      }
    log.info("sendNotifications: Finished sending notifications")
  }

  @Transactional
  fun getNotifications(region: Int) {
    log.info("notifications region: $region")
    try {
      val details = csrClient.getModified(region)

      val cutoffTime = LocalDateTime.now(clock).minusMinutes(CUTOFF_MINUTES)

      val usersWithoutRecentActivity = details
        .groupingBy { it.quantumId }
        .aggregate(::latestShiftModified)
        .filter { it.value.shiftModified?.isBefore(cutoffTime) ?: true }

      // Now omit details for users with recent changes
      val detailsToProcess = details.filter { it.quantumId in usersWithoutRecentActivity.keys }

      val allNotifications = detailsToProcess
        .filter { it.quantumId != null }
        // notify using only the latest add and edit for each day and each user
        .sortedWith(compareBy<CsrModifiedDetailDto> { it.quantumId }.thenByDescending { it.shiftModified })
        .distinctBy {
          CsrModifiedDetailDto(
            quantumId = it.quantumId,
            shiftModified = null,
            shiftType = it.shiftType,
            detailStart = it.detailStart.truncatedTo(ChronoUnit.DAYS),
            detailEnd = it.detailEnd.truncatedTo(ChronoUnit.DAYS),
            activity = null,
            actionType = it.actionType,
          )
        }
        .map {
          // We only want to transform shift level changes, not detail changes.
          if (it.activity == null && it.actionType == DetailModificationType.EDIT && thereIsNoADDForThisShift(it)) {
            it.actionType = DetailModificationType.ADD
          }
          it
        }
        // We want to remove Shift level changes that aren't 'add'
        // we want to filter anything that is unchanged
        .filterNot {
          (it.activity == null && it.actionType == DetailModificationType.EDIT) ||
            it.actionType == DetailModificationType.UNCHANGED ||
            shiftChangeAlreadyRecorded(it)
        }

      log.info("notifications calling saveAll with ${allNotifications.size} notifications for region: $region")
      if (allNotifications.isNotEmpty()) {
        notificationRepository.saveAll(Notification.fromDto(allNotifications))
      }

      if (detailsToProcess.isNotEmpty()) {
        csrClient.deleteProcessed(region, detailsToProcess.mapNotNull { it.id })
      }

      log.info("notifications end for region: $region")
    } catch (e: Exception) {
      log.error("notifications error in region $region", e)
      telemetryClient.trackEvent(
        "notificationsError",
        mapOf("region" to region.toString()),
        null,
      )
    }
  }

  fun tidyNotification() {
    val start = System.currentTimeMillis()
    // Only hold on to 3 months of this temporary data.
    val startOfDay = LocalDate.now(clock).atStartOfDay().minusMonths(3)
    log.info("tidyNotification: Removing old notifications (before $startOfDay)")

    val rows = notificationRepository.deleteAllByShiftModifiedBefore(startOfDay)

    log.info("tidyNotification: Removed old notifications (before $startOfDay)")
    val duration = System.currentTimeMillis() - start
    telemetryClient.trackEvent(
      "tidyNotification",
      mapOf(
        "startOfDay" to "$startOfDay",
        "durationMillis" to duration.toString(),
        "duration" to DurationFormatUtils.formatDuration(duration, "HH:mm:ss"),
        "rowsDeleted" to rows.toString(),
      ),
      null,
    )
  }

  private fun getNotifications(
    quantumId: String,
    start: LocalDateTime,
    end: LocalDateTime,
    unprocessedOnly: Boolean,
  ): Collection<Notification> {
    return notificationRepository.findAllByQuantumIdIgnoreCaseAndShiftModifiedIsBetween(
      quantumId,
      start,
      end,
    )
      .filter { !unprocessedOnly || (unprocessedOnly && !it.processed) }
  }

  private fun thereIsNoADDForThisShift(it: CsrModifiedDetailDto) =
    notificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndActionType(
      it.quantumId!!,
      it.detailStart,
      it.shiftType,
      DetailModificationType.ADD,
    ) == 0

  private fun shiftChangeAlreadyRecorded(it: CsrModifiedDetailDto) =
    (
      it.shiftModified != null &&
        notificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
        it.quantumId!!,
        it.detailStart,
        it.shiftType,
        it.shiftModified,
      ) > 0
      ).also { result -> if (result) log.warn("shiftChangeAlreadyRecorded was true for ${it.quantumId} at ${it.shiftModified}") }
  // This check is probably redundant as triggers should not repeatedly find the same event (unlike the old polling),
  // so if this log message never happens, it can be removed ^

  private fun latestShiftModified(
    @Suppress("UNUSED_PARAMETER") key: String?,
    acc: CsrModifiedDetailDto?,
    item: CsrModifiedDetailDto,
    first: Boolean,
  ): CsrModifiedDetailDto =
    if (first || item.shiftModified == null || acc == null || (acc.shiftModified != null && item.shiftModified.isAfter(acc.shiftModified))) item else acc

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
    val userPreference = userPreferenceService.getUserPreference(quantumId)
    userPreference?.also {
      data class Key(val shiftType: ShiftType, val quantumId: String, val detailStart: LocalDateTime)

      fun Notification.toKeyDuplicates() = Key(this.parentType, this.quantumId.uppercase(), this.detailStart)

      // Only send the latest notification for a shift if there are multiple
      val mostRecentNotifications = notificationGroup
        .groupBy { it.toKeyDuplicates() }
        .map { (_, value) -> value.maxByOrNull { it.shiftModified } }
        .filterNotNull()

      if (shouldSend(userPreference)) {
        log.debug("sendNotification: Sending (${mostRecentNotifications.size}) notifications to ${userPreference.quantumId}, preference set to ${userPreference.commPref}")
        mostRecentNotifications.sortedWith(compareBy { it.detailStart }).chunked(10).forEach { chunk ->
          when (val communicationPreference = userPreference.commPref) {
            CommunicationPreference.EMAIL -> notifyClient.sendEmail(
              NotificationType.EMAIL_SUMMARY.value,
              userPreference.email,
              generateTemplateValues(chunk, communicationPreference),
              null,
            )
            CommunicationPreference.SMS -> notifyClient.sendSms(
              NotificationType.SMS_SUMMARY.value,
              userPreference.sms,
              generateTemplateValues(chunk, communicationPreference),
              null,
            )
            else -> {
              log.info("sendNotification: Skipping sending notifications for ${userPreference.quantumId}")
            }
          }
        }
      }
    }
  }

  private fun shouldSend(userPreference: UserPreference): Boolean {
    val isNotSnoozed = userPreference.snoozeUntil == null || userPreference.snoozeUntil!!.isBefore(LocalDate.now(clock))
    val isValidCommunicationMethod = when (userPreference.commPref) {
      CommunicationPreference.EMAIL -> !userPreference.email.isNullOrBlank()
      CommunicationPreference.SMS -> !userPreference.sms.isNullOrBlank()
      else -> {
        false
      }
    }
    return isNotSnoozed && isValidCommunicationMethod
  }

  private fun generateTemplateValues(
    chunk: List<Notification>,
    communicationPreference: CommunicationPreference,
  ): MutableMap<String, String?> {
    val personalisation = mutableMapOf<String, String?>()
    // Get the oldest modified date "Changes since"
    personalisation["title"] =
      chunk.minByOrNull { it.shiftModified }?.shiftModified?.let { "Changes since ${it.getDateTimeFormattedForTemplate()}" }
    // Map each notification onto an predefined key
    personalisation.putAll(
      notificationKeys
        .mapIndexed { index, templateKey ->
          templateKey to (
            chunk.getOrNull(index)?.getNotificationDescription(communicationPreference)
              ?: ""
            )
        }.toMap(),
    )
    return personalisation
  }

  companion object {
    private val log = LoggerFactory.getLogger(NotificationService::class.java)

    private val notificationKeys =
      listOf("not1", "not2", "not3", "not4", "not5", "not6", "not7", "not8", "not9", "not10")
  }
}
