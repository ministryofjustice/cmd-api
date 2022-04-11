package uk.gov.justice.digital.hmpps.cmd.api.service

import com.microsoft.applicationinsights.TelemetryClient
import com.microsoft.applicationinsights.core.dependencies.google.common.collect.ImmutableMap
import org.apache.commons.lang3.time.DurationFormatUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrClient
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrModifiedDetailDto
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailModificationType
import uk.gov.justice.digital.hmpps.cmd.api.model.DryRunNotification
import uk.gov.justice.digital.hmpps.cmd.api.repository.DryRunNotificationRepository
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import uk.gov.service.notify.NotificationClientApi
import uk.gov.service.notify.NotificationClientException
import java.time.Clock
import java.time.LocalDateTime

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
  private val telemetryClient: TelemetryClient
) {

  @Transactional(propagation = Propagation.NEVER)
  fun dryRunSendNotifications() {
    val start = System.currentTimeMillis()
    val unprocessedNotifications = dryRunNotificationRepository.findAllByProcessedIsFalse()
    log.info("dryRunSendNotifications: Sending notifications, found: ${unprocessedNotifications.size}")
    unprocessedNotifications.groupBy { it.quantumId }
      .forEach { group ->
        try {
          // NO! sendNotification(group.key, group.value)
          group.value.forEach { it.processed = true }
          dryRunNotificationRepository.saveAll(group.value)
        } catch (e: NotificationClientException) {
          log.warn("Sending notifications to user ${group.key} ${group.value} FAILED", e)
        }
        log.info("dryRunSendNotifications: Sent notification (${group.value.size} lines) for ${group.key}")
      }
    log.info("dryRunSendNotifications: Finished sending notifications")
    val duration = System.currentTimeMillis() - start
    telemetryClient.trackEvent(
      "dryRunSendNotifications",
      ImmutableMap.of(
        "unprocessedNotifications", "${unprocessedNotifications.size}",
        "durationMillis", duration.toString(),
        "duration", DurationFormatUtils.formatDuration(duration, "HH:mm:ss")
      ),
      null
    )
  }

  @Transactional
  fun dryRunNotifications(region: Int) {
    val start = System.currentTimeMillis()
    log.info("dryRunNotifications region: $region")

    val details = csrClient.getModified(region)

    log.debug("dryRunNotifications region: $region found ${details.size} cmd_notifications details")

    val cutoffTime = LocalDateTime.now(clock).minusMinutes(CUTOFF_MINUTES)

    val usersWithoutRecentActivity = details
      .groupingBy { it.quantumId }
      .aggregate(::latestShiftModified)
      .filter { it.value.shiftModified.isBefore(cutoffTime) }

    // Now omit details for users with recent changes
    val detailsToProcess = details.filter { it.quantumId in usersWithoutRecentActivity.keys }

    val allNotifications = detailsToProcess
      .distinctBy { it.copy(id = null, shiftModified = LocalDateTime.MIN) } // omit id in comparison
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
      csrClient.deleteProcessed(region, detailsToProcess.map { it.id!! })
    }

    val duration = System.currentTimeMillis() - start
    telemetryClient.trackEvent(
      "dryRunNotifications",
      ImmutableMap.of(
        "region", "$region",
        "durationMillis", duration.toString(),
        "duration", DurationFormatUtils.formatDuration(duration, "HH:mm:ss")
      ),
      null
    )
    log.info("dryRunNotifications end for region: $region")
  }

  private fun thereIsNoADDForThisShift(it: CsrModifiedDetailDto) =
    dryRunNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndActionType(
      it.quantumId, it.detailStart, it.shiftType, DetailModificationType.ADD
    ) == 0

  private fun shiftChangeAlreadyRecorded(it: CsrModifiedDetailDto) =
    dryRunNotificationRepository.countAllByQuantumIdIgnoreCaseAndDetailStartAndParentTypeAndShiftModified(
      it.quantumId, it.detailStart, it.shiftType, it.shiftModified
    ) > 0

  private fun latestShiftModified(
    @Suppress("UNUSED_PARAMETER") key: String,
    acc: CsrModifiedDetailDto?,
    item: CsrModifiedDetailDto,
    first: Boolean
  ): CsrModifiedDetailDto =
    if (first) item else if (item.shiftModified.isAfter(acc!!.shiftModified)) item else acc

  companion object {
    private val log = LoggerFactory.getLogger(DryRunNotificationService::class.java)
  }
}
