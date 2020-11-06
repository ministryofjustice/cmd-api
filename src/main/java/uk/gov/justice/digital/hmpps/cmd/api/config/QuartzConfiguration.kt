package uk.gov.justice.digital.hmpps.cmd.api.config

import org.quartz.JobDetail
import org.quartz.Trigger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.scheduling.quartz.CronTriggerFactoryBean
import org.springframework.scheduling.quartz.JobDetailFactoryBean
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.springframework.scheduling.quartz.SpringBeanJobFactory
import uk.gov.justice.digital.hmpps.cmd.api.controllers.*
import javax.sql.DataSource

@Configuration
@Profile("!dev")
@EnableAutoConfiguration
class QuartzConfiguration(val applicationContext: ApplicationContext) {

    @Bean
    fun notificationRefreshJobR1(): JobDetailFactoryBean {
        val jobDetailFactory = JobDetailFactoryBean()
        jobDetailFactory.setJobClass(NotificationRefreshQuartzJobR1::class.java)
        jobDetailFactory.setDescription("Invoke Notification Refresh Job Region 1...")
        jobDetailFactory.setDurability(true)
        return jobDetailFactory
    }

    @Bean
    fun notificationRefreshTriggerR1(@Qualifier("notificationRefreshJobR1") job: JobDetail): CronTriggerFactoryBean {
        val trigger = CronTriggerFactoryBean()
        trigger.setJobDetail(job)
        trigger.setCronExpression("0 5 */3 ? * *")
        trigger.setMisfireInstruction(2) // Do Nothing
        return trigger
    }

    @Bean
    fun notificationRefreshJobR2(): JobDetailFactoryBean {
        val jobDetailFactory = JobDetailFactoryBean()
        jobDetailFactory.setJobClass(NotificationRefreshQuartzJobR2::class.java)
        jobDetailFactory.setDescription("Invoke Notification Refresh Job Region 2...")
        jobDetailFactory.setDurability(true)
        return jobDetailFactory
    }

    @Bean
    fun notificationRefreshTriggerR2(@Qualifier("notificationRefreshJobR2") job: JobDetail): CronTriggerFactoryBean {
        val trigger = CronTriggerFactoryBean()
        trigger.setJobDetail(job)
        trigger.setCronExpression("0 10 */3 ? * *")
        trigger.setMisfireInstruction(2) // Do Nothing
        return trigger
    }

    @Bean
    fun notificationRefreshJobR3(): JobDetailFactoryBean {
        val jobDetailFactory = JobDetailFactoryBean()
        jobDetailFactory.setJobClass(NotificationRefreshQuartzJobR3::class.java)
        jobDetailFactory.setDescription("Invoke Notification Refresh Job Region 3...")
        jobDetailFactory.setDurability(true)
        return jobDetailFactory
    }

    @Bean
    fun notificationRefreshTriggerR3(@Qualifier("notificationRefreshJobR3") job: JobDetail): CronTriggerFactoryBean {
        val trigger = CronTriggerFactoryBean()
        trigger.setJobDetail(job)
        trigger.setCronExpression("0 15 */3 ? * *")
        trigger.setMisfireInstruction(2) // Do Nothing
        return trigger
    }

    @Bean
    fun notificationRefreshJobR4(): JobDetailFactoryBean {
        val jobDetailFactory = JobDetailFactoryBean()
        jobDetailFactory.setJobClass(NotificationRefreshQuartzJobR4::class.java)
        jobDetailFactory.setDescription("Invoke Notification Refresh Job Region 4...")
        jobDetailFactory.setDurability(true)
        return jobDetailFactory
    }

    @Bean
    fun notificationRefreshTriggerR4(@Qualifier("notificationRefreshJobR4") job: JobDetail): CronTriggerFactoryBean {
        val trigger = CronTriggerFactoryBean()
        trigger.setJobDetail(job)
        trigger.setCronExpression("0 20 */3 ? * *")
        trigger.setMisfireInstruction(2) // Do Nothing
        return trigger
    }

    @Bean
    fun notificationRefreshJobR5(): JobDetailFactoryBean {
        val jobDetailFactory = JobDetailFactoryBean()
        jobDetailFactory.setJobClass(NotificationRefreshQuartzJobR5::class.java)
        jobDetailFactory.setDescription("Invoke Notification Refresh Job Region 5...")
        jobDetailFactory.setDurability(true)
        return jobDetailFactory
    }

    @Bean
    fun notificationRefreshTriggerR5(@Qualifier("notificationRefreshJobR5") job: JobDetail): CronTriggerFactoryBean {
        val trigger = CronTriggerFactoryBean()
        trigger.setJobDetail(job)
        trigger.setCronExpression("0 25 */3 ? * *")
        trigger.setMisfireInstruction(2) // Do Nothing
        return trigger
    }

    @Bean
    fun notificationRefreshJobR6(): JobDetailFactoryBean {
        val jobDetailFactory = JobDetailFactoryBean()
        jobDetailFactory.setJobClass(NotificationRefreshQuartzJobR6::class.java)
        jobDetailFactory.setDescription("Invoke Notification Refresh Job Region 6...")
        jobDetailFactory.setDurability(true)
        return jobDetailFactory
    }

    @Bean
    fun notificationRefreshTriggerR6(@Qualifier("notificationRefreshJobR6") job: JobDetail): CronTriggerFactoryBean {
        val trigger = CronTriggerFactoryBean()
        trigger.setJobDetail(job)
        trigger.setCronExpression("0 30 */3 ? * *")
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
        trigger.setCronExpression("0 4 0 ? * *")
        trigger.setMisfireInstruction(2) // Do Nothing
        return trigger
    }

    @Bean
    fun notificationRefreshSchedulerR1(@Qualifier("notificationRefreshTriggerR1") trigger: Trigger, @Qualifier("notificationRefreshJobR1") job: JobDetail, quartzDataSource: DataSource): SchedulerFactoryBean? {
        val schedulerFactory = SchedulerFactoryBean()
        schedulerFactory.setConfigLocation(ClassPathResource("quartz.properties"))
        schedulerFactory.setJobFactory(springBeanJobFactory())
        schedulerFactory.setJobDetails(job)
        schedulerFactory.setTriggers(trigger)
        schedulerFactory.setDataSource(quartzDataSource)
        return schedulerFactory
    }

    @Bean
    fun notificationRefreshSchedulerR2(@Qualifier("notificationRefreshTriggerR2") trigger: Trigger, @Qualifier("notificationRefreshJobR2") job: JobDetail, quartzDataSource: DataSource): SchedulerFactoryBean? {
        val schedulerFactory = SchedulerFactoryBean()
        schedulerFactory.setConfigLocation(ClassPathResource("quartz.properties"))
        schedulerFactory.setJobFactory(springBeanJobFactory())
        schedulerFactory.setJobDetails(job)
        schedulerFactory.setTriggers(trigger)
        schedulerFactory.setDataSource(quartzDataSource)
        return schedulerFactory
    }

    @Bean
    fun notificationRefreshSchedulerR3(@Qualifier("notificationRefreshTriggerR3") trigger: Trigger, @Qualifier("notificationRefreshJobR3") job: JobDetail, quartzDataSource: DataSource): SchedulerFactoryBean? {
        val schedulerFactory = SchedulerFactoryBean()
        schedulerFactory.setConfigLocation(ClassPathResource("quartz.properties"))
        schedulerFactory.setJobFactory(springBeanJobFactory())
        schedulerFactory.setJobDetails(job)
        schedulerFactory.setTriggers(trigger)
        schedulerFactory.setDataSource(quartzDataSource)
        return schedulerFactory
    }

    @Bean
    fun notificationRefreshSchedulerR4(@Qualifier("notificationRefreshTriggerR4") trigger: Trigger, @Qualifier("notificationRefreshJobR4") job: JobDetail, quartzDataSource: DataSource): SchedulerFactoryBean? {
        val schedulerFactory = SchedulerFactoryBean()
        schedulerFactory.setConfigLocation(ClassPathResource("quartz.properties"))
        schedulerFactory.setJobFactory(springBeanJobFactory())
        schedulerFactory.setJobDetails(job)
        schedulerFactory.setTriggers(trigger)
        schedulerFactory.setDataSource(quartzDataSource)
        return schedulerFactory
    }

    @Bean
    fun notificationRefreshSchedulerR5(@Qualifier("notificationRefreshTriggerR5") trigger: Trigger, @Qualifier("notificationRefreshJobR5") job: JobDetail, quartzDataSource: DataSource): SchedulerFactoryBean? {
        val schedulerFactory = SchedulerFactoryBean()
        schedulerFactory.setConfigLocation(ClassPathResource("quartz.properties"))
        schedulerFactory.setJobFactory(springBeanJobFactory())
        schedulerFactory.setJobDetails(job)
        schedulerFactory.setTriggers(trigger)
        schedulerFactory.setDataSource(quartzDataSource)
        return schedulerFactory
    }

    @Bean
    fun notificationRefreshSchedulerR6(@Qualifier("notificationRefreshTriggerR6") trigger: Trigger, @Qualifier("notificationRefreshJobR6") job: JobDetail, quartzDataSource: DataSource): SchedulerFactoryBean? {
        val schedulerFactory = SchedulerFactoryBean()
        schedulerFactory.setConfigLocation(ClassPathResource("quartz.properties"))
        schedulerFactory.setJobFactory(springBeanJobFactory())
        schedulerFactory.setJobDetails(job)
        schedulerFactory.setTriggers(trigger)
        schedulerFactory.setDataSource(quartzDataSource)
        return schedulerFactory
    }

    @Bean
    fun notificationTidyUpScheduler(@Qualifier("notificationTidyUpTrigger") trigger: Trigger, @Qualifier("notificationTidyUpJob") job: JobDetail, quartzDataSource: DataSource): SchedulerFactoryBean? {
        val schedulerFactory = SchedulerFactoryBean()
        schedulerFactory.setConfigLocation(ClassPathResource("quartz.properties"))
        schedulerFactory.setJobFactory(springBeanJobFactory())
        schedulerFactory.setJobDetails(job)
        schedulerFactory.setTriggers(trigger)
        schedulerFactory.setDataSource(quartzDataSource)
        return schedulerFactory
    }

    @Bean
    fun notificationSendJob(): JobDetailFactoryBean? {
        val jobDetailFactory = JobDetailFactoryBean()
        jobDetailFactory.setJobClass(NotificationSendQuartzJob::class.java)
        jobDetailFactory.setDescription("Invoke Notification Send Job...")
        jobDetailFactory.setDurability(true)
        return jobDetailFactory
    }

    @Bean
    fun notificationSendTrigger(@Qualifier("notificationSendJob") job: JobDetail): CronTriggerFactoryBean {
        val trigger = CronTriggerFactoryBean()
        trigger.setJobDetail(job)
        trigger.setCronExpression("0 0 9,18,21 ? * *")
        trigger.setMisfireInstruction(1) // Fire Again
        return trigger
    }

    @Bean
    fun notificationSendScheduler(@Qualifier("notificationSendTrigger") trigger: Trigger, @Qualifier("notificationSendJob") job: JobDetail, quartzDataSource: DataSource): SchedulerFactoryBean? {
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
