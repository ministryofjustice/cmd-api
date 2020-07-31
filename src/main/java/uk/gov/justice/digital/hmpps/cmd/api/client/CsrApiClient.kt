package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.client

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.reactive.function.client.WebClient

class CsrApiClient(@Qualifier("csrApiWebClient") private val webClient: WebClient) {

    fun getShifts() {}

    fun getDetails() {}

    fun getOvertimeShifts() {}

    fun getOvertimeDetails() {}
}