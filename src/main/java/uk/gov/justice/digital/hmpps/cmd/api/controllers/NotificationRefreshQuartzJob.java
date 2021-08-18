package uk.gov.justice.digital.hmpps.cmd.api.controllers;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.hmpps.cmd.api.service.NotificationService;

@Component
public class NotificationRefreshQuartzJob implements Job {

    @Autowired
    private NotificationService service;

    public void execute(JobExecutionContext context) {
        for (int region = 1; region <= 6; region++) {
            service.refreshNotifications(region);
            service.sendNotifications();
        }
    }
}
