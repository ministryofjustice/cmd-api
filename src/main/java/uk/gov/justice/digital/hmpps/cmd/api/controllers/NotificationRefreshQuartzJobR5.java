package uk.gov.justice.digital.hmpps.cmd.api.controllers;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.hmpps.cmd.api.service.NotificationService;

@Component
public class NotificationRefreshQuartzJobR5 implements Job {

    @Autowired
    private NotificationService service;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        service.refreshNotifications(5);
    }

}
