package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.scheduler

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrClient
import uk.gov.justice.digital.hmpps.cmd.api.service.DryRunNotificationService

@Component
class PollingScheduler(
  private val csrClient: CsrClient,
  private val dryRunnotificationService: DryRunNotificationService
) {
  @Scheduled(
    cron = "17 */10 7-19 * * ?"
  )
  @SchedulerLock(
    name = "pollNotificationsLock",
    lockAtLeastFor = "PT9M"
  ) // lock for slightly less than polling interval
  fun pollNotifications() {
    log.info("pollNotifications start")

    for (region in 1..6) {
      dryRunnotificationService.dryRunNotifications(region)
    }
    dryRunnotificationService.dryRunSendNotifications()

    log.info("pollNotifications end")
  }

  companion object {
    private val log = LoggerFactory.getLogger(PollingScheduler::class.java)
  }
}
