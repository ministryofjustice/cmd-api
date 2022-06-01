package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.scheduler

import com.microsoft.applicationinsights.TelemetryClient
import com.microsoft.applicationinsights.core.dependencies.google.common.collect.ImmutableMap
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.apache.commons.lang3.time.DurationFormatUtils
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.cmd.api.service.DryRunNotificationService

const val INTERVAL = 10
const val MIN_LOCK = INTERVAL - 1 // lock for slightly less than polling interval

@Component
class PollingScheduler(
  private val dryRunnotificationService: DryRunNotificationService,
  private val telemetryClient: TelemetryClient,
) {
  @Scheduled(cron = "17 */$INTERVAL 7-19 * * ?")
  @SchedulerLock(
    name = "pollNotificationsLock",
    lockAtLeastFor = "PT${MIN_LOCK}M"
  )
  fun pollNotifications() {
    log.info("pollNotifications start")
    val start = System.currentTimeMillis()

    for (region in 1..6) {
      dryRunnotificationService.dryRunNotifications(region)
    }
    dryRunnotificationService.sendNotifications()

    val duration = System.currentTimeMillis() - start
    telemetryClient.trackEvent(
      "PollingScheduler",
      ImmutableMap.of(
        "durationMillis", duration.toString(),
        "duration", DurationFormatUtils.formatDuration(duration, "HH:mm:ss")
      ),
      null
    )
    log.info("pollNotifications end")
  }

  companion object {
    private val log = LoggerFactory.getLogger(PollingScheduler::class.java)
  }
}
