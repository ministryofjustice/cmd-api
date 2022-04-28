package uk.gov.justice.digital.hmpps.cmd.api

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling
import uk.gov.service.notify.NotificationClient
import uk.gov.service.notify.NotificationClientApi
import java.time.Clock

@EnableCaching
@EnableScheduling
@SpringBootApplication
class CmdApiApplication {

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      SpringApplication.run(CmdApiApplication::class.java, *args)
    }
  }

  @Bean
  fun initialiseClock(): Clock {
    return Clock.systemDefaultZone()
  }

  @Bean
  fun notificationClient(@Value("\${application.notify.key}") key: String?): NotificationClientApi? {
    return NotificationClient(key)
  }
}
