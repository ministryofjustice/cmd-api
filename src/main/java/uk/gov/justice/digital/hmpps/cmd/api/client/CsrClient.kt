package uk.gov.justice.digital.hmpps.cmd.api.client

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import uk.gov.justice.digital.hmpps.cmd.api.utils.region.Regions
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class CsrClient(@Qualifier("csrApiWebClient") val csrClient: WebClient, val authenticationFacade: AuthenticationFacade, val regionData: Regions, @Value("\${jwt.secret}") val secret: String) {

    fun getDetailsForUser(from: LocalDate, to: LocalDate, region: Int?) : Collection<CsrDetailDto> {
        if(region != null) {
             return getDetails(from, to, region.toString())
        } else {
            // Fallback to checking each region until we get some results.
            regionData.regions.forEach {
                val data = getDetails(from, to, it.name)
                if(!data.isEmpty()) {
                    return data
                }
            }
        }
        return listOf()
    }

    fun getModifiedDetails(planUnit: String, region: Int): Collection<CsrModifiedDetailDto> {
        log.info("Finding shift notifications, PlanUnit $planUnit, Region $region")
        val responseType = object : ParameterizedTypeReference<Collection<CsrModifiedDetailDto>>() {}
        val csrModifiedDetails : Collection<CsrModifiedDetailDto> = csrClient
                .get()
                .uri("/planUnit/${planUnit}/detail/modified")
                .header("X-Region", region.toString())
                .retrieve()
                .bodyToMono(responseType)
                .block() ?: listOf()
        log.info("Found ${csrModifiedDetails.size} shift notifications, PlanUnit $planUnit, Region $region")

        return csrModifiedDetails
    }

    private fun getDetails(from: LocalDate, to: LocalDate, region: String) : Collection<CsrDetailDto> {
        log.debug("Finding details for ${authenticationFacade.currentUsername}, Region $region")
        val responseType = object : ParameterizedTypeReference<Collection<CsrDetailDto>>() {}
        val csrDetails : Collection<CsrDetailDto> = csrClient
                .get()
                .uri("/user/detail?from=$from&to=$to")
                .header("X-Region", region)
                .retrieve()
                .bodyToMono(responseType)
                .block() ?: listOf()
        log.info("Found ${csrDetails.size} details for ${authenticationFacade.currentUsername}, Region $region")

        return csrDetails
    }

    companion object {
        private val log = LoggerFactory.getLogger(CsrClient::class.java)
    }
}

data class CsrDetailDto @JsonCreator constructor(
        @JsonProperty("shiftDate")
        var shiftDate: LocalDate,

        @JsonProperty("entityType")
        var entityType: String,

        @JsonProperty("detailStart")
        var start: Long,

        @JsonProperty("detailEnd")
        var end: Long,

        @JsonProperty("detailType")
        var detailType: String,

        @JsonProperty("activity")
        var activity: String
)

data class CsrModifiedDetailDto @JsonCreator constructor(

        @JsonProperty("quantumId")
        var quantumId: String,

        @JsonProperty("shiftModified")
        var shiftModified: LocalDateTime,

        @JsonProperty("shiftDate")
        var shiftDate: LocalDate,

        @JsonProperty("shiftType")
        var shiftType: String,

        @JsonProperty("detailStart")
        var detailStart: Long?,

        @JsonProperty("detailEnd")
        var detailEnd: Long?,

        @JsonProperty("activity")
        var activity: String?,

        @JsonProperty("actionType")
        var actionType: String
)