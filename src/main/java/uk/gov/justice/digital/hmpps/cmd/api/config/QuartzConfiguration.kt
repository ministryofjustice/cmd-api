package uk.gov.justice.digital.hmpps.cmd.api.config

import org.quartz.JobDetail
import org.quartz.Trigger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ClassPathResource
import org.springframework.scheduling.quartz.CronTriggerFactoryBean
import org.springframework.scheduling.quartz.JobDetailFactoryBean
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.springframework.scheduling.quartz.SpringBeanJobFactory
import uk.gov.justice.digital.hmpps.cmd.api.controllers.NotificationRefreshQuartzJob
import uk.gov.justice.digital.hmpps.cmd.api.controllers.NotificationTidyUpQuartzJob
import javax.sql.DataSource

@Configuration
@Profile("!dev")
@EnableAutoConfiguration
class QuartzConfiguration(val applicationContext: ApplicationContext) {

  @Bean
  fun notificationRefreshJob(): JobDetailFactoryBean {
    val jobDetailFactory = JobDetailFactoryBean()
    jobDetailFactory.setJobClass(NotificationRefreshQuartzJob::class.java)
    jobDetailFactory.setDescription("Invoke Notification Refresh Job ...")
    jobDetailFactory.setDurability(true)
    return jobDetailFactory
  }

  @Bean
  fun notificationRefreshTrigger(@Qualifier("notificationRefreshJob") job: JobDetail): CronTriggerFactoryBean {
    val trigger = CronTriggerFactoryBean()
    trigger.setJobDetail(job)
    trigger.setCronExpression("0 30 4,8,12,17 ? * *")
    trigger.setMisfireInstruction(2) // Do Nothing
    return trigger
  }

  @Bean
  fun notificationTidyUpJob(): JobDetailFactoryBean {
    val jobDetailFactory = JobDetailFactoryBean()
    jobDetailFactory.setJobClass(NotificationTidyUpQuartzJob::class.java)
    jobDetailFactory.setDescription("Invoke Notification Tidy Up Job..")
    jobDetailFactory.setDurability(true)
    return jobDetailFactory
  }

  @Bean
  fun notificationTidyUpTrigger(@Qualifier("notificationTidyUpJob") job: JobDetail): CronTriggerFactoryBean {
    val trigger = CronTriggerFactoryBean()
    trigger.setJobDetail(job)
    trigger.setCronExpression("0 0 4 * * ?")
    trigger.setMisfireInstruction(2) // Do Nothing
    return trigger
  }

  @Bean
  fun notificationRefreshScheduler(
    @Qualifier("notificationRefreshTrigger") trigger: Trigger,
    @Qualifier("notificationRefreshJob") job: JobDetail,
    quartzDataSource: DataSource
  ): SchedulerFactoryBean {
    val schedulerFactory = SchedulerFactoryBean()
    schedulerFactory.setConfigLocation(ClassPathResource("quartz.properties"))
    schedulerFactory.setJobFactory(springBeanJobFactory())
    schedulerFactory.setJobDetails(job)
    schedulerFactory.setTriggers(trigger)
    schedulerFactory.setDataSource(quartzDataSource)
    return schedulerFactory
  }

  @Bean
  fun notificationTidyUpScheduler(
    @Qualifier("notificationTidyUpTrigger") trigger: Trigger,
    @Qualifier("notificationTidyUpJob") job: JobDetail,
    quartzDataSource: DataSource
  ): SchedulerFactoryBean {
    val schedulerFactory = SchedulerFactoryBean()
    schedulerFactory.setConfigLocation(ClassPathResource("quartz.properties"))
    schedulerFactory.setJobFactory(springBeanJobFactory())
    schedulerFactory.setJobDetails(job)
    schedulerFactory.setTriggers(trigger)
    schedulerFactory.setDataSource(quartzDataSource)
    return schedulerFactory
  }

  @Bean
  fun springBeanJobFactory(): SpringBeanJobFactory {
    val jobFactory = AutowiringSpringBeanJobFactory()
    jobFactory.setApplicationContext(applicationContext)
    return jobFactory
  }
}
