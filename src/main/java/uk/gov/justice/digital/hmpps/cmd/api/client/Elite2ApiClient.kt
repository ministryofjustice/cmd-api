package uk.gov.justice.digital.hmpps.cmd.api.client

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class Elite2ApiClient(@Qualifier("elite2ApiWebClient") private val webClient: WebClient) {

    fun getCurrentPrison() : CaseLoad {
        return webClient.get()
                .uri("https://api-dev.prison.service.justice.gov.uk/api/users/me")
                .retrieve()
                .bodyToMono(CaseLoad::class.java)
                .block()!!
    }
}

data class CaseLoad(

        @JsonProperty("activeCaseLoadId")
        val activeCaseLoadId : String
) 