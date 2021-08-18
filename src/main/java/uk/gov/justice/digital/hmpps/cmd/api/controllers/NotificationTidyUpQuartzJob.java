package uk.gov.justice.digital.hmpps.cmd.api.controllers;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.hmpps.cmd.api.service.NotificationService;

@Component
public class NotificationTidyUpQuartzJob implements Job {

    @Autowired
    private NotificationService service;

    public void execute(JobExecutionContext context) {
        service.tidyNotification();
    }
}
