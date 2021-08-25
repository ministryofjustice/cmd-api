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
        service.refreshNotifications(6);
        service.sendNotifications();
        service.refreshNotifications(1);
        service.sendNotifications();
        service.refreshNotifications(2);
        service.sendNotifications();
        service.refreshNotifications(3);
        service.sendNotifications();
        service.refreshNotifications(4);
        service.sendNotifications();
        service.refreshNotifications(5);
        service.sendNotifications();
    }
}
