package uk.gov.justice.digital.hmpps.cmd.api.scheduler

import com.microsoft.applicationinsights.TelemetryClient
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.apache.commons.lang3.time.DurationFormatUtils
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.cmd.api.service.NotificationService

const val INTERVAL = 10
const val MIN_LOCK = INTERVAL - 1 // lock for slightly less than polling interval

@Component
class PollingScheduler(
  private val notificationService: NotificationService,
  private val telemetryClient: TelemetryClient,
) {
  @Scheduled(cron = "17 */$INTERVAL 6-21 * * ?")
  @SchedulerLock(
    name = "pollNotificationsLock",
    lockAtLeastFor = "PT${MIN_LOCK}M"
  )
  fun pollNotifications() {
    log.info("pollNotifications start")
    val start = System.currentTimeMillis()

    for (region in 1..6) {
      notificationService.getNotifications(region)
    }
    notificationService.sendNotifications()

    val duration = System.currentTimeMillis() - start
    telemetryClient.trackEvent(
      "PollingScheduler",
      mapOf(
        "durationMillis" to duration.toString(),
        "duration" to DurationFormatUtils.formatDuration(duration, "HH:mm:ss")
      ),
      null
    )
    log.info("pollNotifications end")
  }

  @Scheduled(cron = "0 37 3 * * ?")
  @SchedulerLock(
    name = "tidyNotificationsLock",
    lockAtLeastFor = "PT10M"
  )
  fun tidyNotifications() {
    log.info("tidyNotifications start")
    val start = System.currentTimeMillis()

    notificationService.tidyNotification()

    val duration = System.currentTimeMillis() - start
    telemetryClient.trackEvent(
      "PollingSchedulerTidy",
      mapOf(
        "durationMillis" to duration.toString(),
        "duration" to DurationFormatUtils.formatDuration(duration, "HH:mm:ss")
      ),
      null
    )
    log.info("tidyNotifications end")
  }

  companion object {
    private val log = LoggerFactory.getLogger(PollingScheduler::class.java)
  }
}
