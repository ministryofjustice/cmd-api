package uk.gov.justice.digital.hmpps.cmd.api

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class CmdApiApplication {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(CmdApiApplication::class.java, *args)
        }
    }
}