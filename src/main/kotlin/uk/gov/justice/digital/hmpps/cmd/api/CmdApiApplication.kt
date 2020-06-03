package uk.gov.justice.digital.hmpps.cmd.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CmdApiApplication

fun main(args: Array<String>) {
	runApplication<CmdApiApplication>(*args)
}
