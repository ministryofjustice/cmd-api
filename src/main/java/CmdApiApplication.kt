package uk.gov.justice.digital.hmpps.cmd.api

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import uk.gov.service.notify.NotificationClient
import uk.gov.service.notify.NotificationClientApi
import java.time.Clock


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

    @Bean
    @Primary
    fun objectMapper(): ObjectMapper? {
        return ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
                .registerModules(Jdk8Module(), JavaTimeModule(), KotlinModule())
    }
}