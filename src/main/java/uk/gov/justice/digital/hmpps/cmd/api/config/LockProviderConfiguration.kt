package uk.gov.justice.digital.hmpps.cmd.api.config

import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
class LockProviderConfiguration {
  @Bean
  fun lockProvider(jdbcTemplate: JdbcTemplate): LockProvider = JdbcTemplateLockProvider(
    JdbcTemplateLockProvider.Configuration.builder()
      .withJdbcTemplate(jdbcTemplate)
      .usingDbTime()
      .build(),
  )
}

@Configuration
@Profile("!test") // prevent scheduler running during integration tests
@EnableScheduling
@EnableSchedulerLock(defaultLockAtLeastFor = "PT1M", defaultLockAtMostFor = "PT1H")
class SpringSchedulingConfig
