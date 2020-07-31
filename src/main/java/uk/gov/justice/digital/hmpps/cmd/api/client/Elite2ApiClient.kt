package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.client

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.reactive.function.client.WebClient

class Elite2ApiClient(@Qualifier("elite2ApiWebClient") private val webClient: WebClient) {

    fun getCurrentPrison() : CaseLoad {
        return webClient.get()
                .uri("/api/users/me")
                .retrieve()
                .bodyToMono(CaseLoad::class.java)
                .block()!!
    }
}

data class CaseLoad(

        @JsonProperty("activeCaseLoadId")
        val activeCaseLoadId : String
)