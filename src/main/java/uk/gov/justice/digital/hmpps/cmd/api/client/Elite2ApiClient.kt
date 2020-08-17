package uk.gov.justice.digital.hmpps.cmd.api.client

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade

@Component
class Elite2ApiClient(@Qualifier("elite2ApiWebClient") private val webClient: WebClient, private val authenticationFacade: AuthenticationFacade) {

    fun getCurrentPrison() : CaseLoad {
        val dsfd = webClient.get()
                .retrieve()
                .bodyToMono(CaseLoad::class.java)
                .block()!!
        return CaseLoad("dsfd")
    }
}

data class CaseLoad(

        @JsonProperty("activeCaseLoadId")
        val activeCaseLoadId : String
) 