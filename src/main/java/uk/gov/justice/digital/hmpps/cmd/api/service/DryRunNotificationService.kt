package uk.gov.justice.digital.hmpps.cmd.api.service

import com.microsoft.applicationinsights.TelemetryClient
import com.microsoft.applicationinsights.core.dependencies.google.common.collect.ImmutableMap
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
import uk.gov.justice.digital.hmpps.cmd.api.model.DryRunNotification
import uk.gov.justice.digital.hmpps.cmd.api.model.DryRunNotification.Companion.getDateTimeFormattedForTemplate
import uk.gov.justice.digital.hmpps.cmd.api.model.UserPreference
import uk.gov.justice.digital.hmpps.cmd.api.repository.DryRunNotificationRepository
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import uk.gov.service.notify.NotificationClientApi
import uk.gov.service.notify.NotificationClientException
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

private const val CUTOFF_MINUTES = 5L

@Service
@Transactional // all public methods do updates, including getNotifications() !
class DryRunNotificationService(
  private val dryRunNotificationRepository: DryRunNotificationRepository,
  private val userPreferenceService: UserPreferenceService,
  private val clock: Clock,
  private val authenticationFacade: AuthenticationFacade,
  @Value("\${application.to.defaultMonths}")
  private val monthStep: Long,
  private val notifyClient: NotificationClientApi,
  private val prisonService: PrisonService,
  private val csrClient: CsrClient,
  private val telemetryClient: TelemetryClient,
) {

  @Transactional(propagation = Propagation.NEVER)
  fun sendNotifications() {
    val unprocessedNotifications = dryRunNotificationRepository.findAllByProcessedIsFalse()
    log.info("dryRun sendNotifications: Sending notifications, found: ${unprocessedNotifications.size}")
    unprocessedNotifications.groupBy { it.quantumId }
      .forEach { group ->
        try {
          sendNotification(group.key, group.value)
          group.value.forEach { it.processed = true }
          dryRunNotificationRepository.saveAll(group.value)
          log.info("dryRun sendNotifications: Sent notification (${group.value.size} lines) for ${group.key}")
        } catch (e: NotificationClientException) {
          log.warn("dryRun sendNotifications: Sending notifications to user ${group.key} ${group.value} FAILED", e)
        }
      }
    log.info("dryRun sendNotifications: Finished sending notifications")
  }

  @Transactional
  fun dryRunNotifications(region: Int) {
    log.info("dryRunNotifications region: $region")
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

      log.info("dryRunNotifications calling saveAll with ${allNotifications.size} notifications for region: $region")
      if (allNotifications.isNotEmpty()) {
        dryRunNotificationRepository.saveAll(DryRunNotification.fromDto(allNotifications))
      }

      if (detailsToProcess.isNotEmpty()) {
        csrClient.deleteProcessed(region, detailsToProcess.mapNotNull { it.id })
      }

      log.info("dryRunNotifications end for region: $region")
    } catch (e: Exception) {
      log.error("dryRunNotifications error in region $region", e)
      telemetryClient.trackEvent(
        "dryRunNotificationsError",
        ImmutableMap.of("region", region.toString()),
        null
      )
    }
  }

  private fun thereIsNoADDForThisShift(it: CsrModifiedDetailDto) =
    dryRunNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndActionType(
      it.quantumId!!, it.detailStart, it.shiftType, DetailModificationType.ADD
    ) == 0

  private fun shiftChangeAlreadyRecorded(it: CsrModifiedDetailDto) =
    it.shiftModified != null &&
      dryRunNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
      it.quantumId!!, it.detailStart, it.shiftType, it.shiftModified
    ) > 0

  private fun latestShiftModified(
    @Suppress("UNUSED_PARAMETER") key: String?,
    acc: CsrModifiedDetailDto?,
    item: CsrModifiedDetailDto,
    first: Boolean
  ): CsrModifiedDetailDto =
    if (first || item.shiftModified == null || acc == null || (acc.shiftModified != null && item.shiftModified.isAfter(acc.shiftModified))) item else acc

  /*
  * Chunk the notifications into 10s -
  * Notify doesn't support vertical lists
  * so we have to have a fixed size template with 'slots'
  * 10 means we can cover 99.9% of scenarios in one email.
  */
  private fun sendNotification(quantumId: String, notificationGroup: List<DryRunNotification>) {
    val userPreference = userPreferenceService.getUserPreference(quantumId)
    userPreference?.also {
      data class Key(val shiftType: ShiftType, val quantumId: String, val detailStart: LocalDateTime)

      fun DryRunNotification.toKeyDuplicates() = Key(this.parentType, this.quantumId.uppercase(), this.detailStart)

      // Only send the latest notification for a shift if there are multiple
      val mostRecentNotifications = notificationGroup
        .groupBy { it.toKeyDuplicates() }
        .map { (_, value) -> value.maxByOrNull { it.shiftModified } }
        .filterNotNull()

      if (shouldSend(userPreference)) {
        log.debug("dryRun sendNotification: Sending (${mostRecentNotifications.size}) notifications to ${userPreference.quantumId}, preference set to ${userPreference.commPref}")
        mostRecentNotifications.sortedWith(compareBy { it.detailStart }).chunked(10).forEach { chunk ->
          when (val communicationPreference = userPreference.commPref) {
            CommunicationPreference.EMAIL -> {
              notifyClient.sendEmail(
                NotificationType.EMAIL_SUMMARY.value,
                userPreference.email,
                generateTemplateValues(chunk, communicationPreference),
                null
              )
            }
            CommunicationPreference.SMS -> {
              notifyClient.sendSms(
                NotificationType.SMS_SUMMARY.value,
                userPreference.sms,
                generateTemplateValues(chunk, communicationPreference),
                null
              )
            }
            else -> {
              log.info("dryRun sendNotification: Skipping sending notifications for ${userPreference.quantumId}")
            }
          }
        }
      }
    }
  }

  private fun shouldSend(userPreference: UserPreference): Boolean {
    val isNotSnoozed = userPreference.snoozeUntil == null || userPreference.snoozeUntil!!.isBefore(LocalDate.now(clock))
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

  private fun generateTemplateValues(
    chunk: List<DryRunNotification>,
    communicationPreference: CommunicationPreference
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
        }.toMap()
    )
    return personalisation
  }

  companion object {
    private val log = LoggerFactory.getLogger(DryRunNotificationService::class.java)

    private val notificationKeys =
      listOf("not1", "not2", "not3", "not4", "not5", "not6", "not7", "not8", "not9", "not10")
  }
}
