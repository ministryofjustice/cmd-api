package uk.gov.justice.digital.hmpps.cmd.api.client

import com.fasterxml.jackson.annotation.JsonCreator
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailModificationType
import uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.cmd.api.utils.region.Regions
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class CsrClient(
  @Qualifier("csrApiWebClient") private val csrClient: WebClient,
  @Qualifier("csrAPIWebClientAppScope") private val csrApiServiceAccountWebClient: WebClient,
  private val authenticationFacade: HmppsAuthenticationHolder,
  private val regionData: Regions,
  @Value("\${csr.timeout}") private val csrApiTimeout: Duration,
) {

  @Cacheable(
    value = ["userDetails"],
    unless = "#result.size() == 0",
    key = "{ #from.toEpochDay().toString(), #to.toEpochDay().toString(), #quantumId }",
  )
  fun getDetailsForUser(from: LocalDate, to: LocalDate, region: Int?, quantumId: String): Collection<CsrDetailDto> {
    if (region != null) {
      return getDetails(from, to, region.toString())
    } else {
      // Fallback to checking each region until we get some results.
      regionData.regions.forEach {
        val data = getDetails(from, to, it.name)
        if (!data.isEmpty()) {
          return data
        }
      }
    }
    return listOf()
  }

  fun getModified(region: Int): List<CsrModifiedDetailDto> {
    val csrModifiedDetails: List<CsrModifiedDetailDto> = csrApiServiceAccountWebClient
      .get()
      .uri("/updates/{region}", region)
      .retrieve()
      .bodyToMono(object : ParameterizedTypeReference<List<CsrModifiedDetailDto>>() {})
      .timeout(csrApiTimeout, Mono.just(emptyList()))
      .block() ?: emptyList()
    log.info("getModified: found ${csrModifiedDetails.size}, Region $region")
    return csrModifiedDetails
  }

  fun deleteProcessed(region: Int, ids: List<Long>) {
    log.info("deleteProcessed: Region $region")

    csrApiServiceAccountWebClient
      .put()
      .uri("/updates/{region}", region)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(ids)
      .retrieve()
      .bodyToMono(Unit::class.java)
      .timeout(csrApiTimeout)
      .block()

    log.info("deleteProcessed: end, Region $region")
  }

  private fun getDetails(from: LocalDate, to: LocalDate, region: String): Collection<CsrDetailDto> {
    log.debug("User Details: finding User ${authenticationFacade.username}, Region $region")
    val csrDetails: Collection<CsrDetailDto> = csrClient
      .get()
      .uri("/user/details/{region}?from={from}&to={to}", region, from, to)
      .retrieve()
      .bodyToMono(CSR_DETAIL_DTO_LIST_TYPE)
      .timeout(csrApiTimeout, Mono.just(emptyList()))
      .block() ?: listOf()
    log.info("User Details: found ${csrDetails.size}, User ${authenticationFacade.username}, $from - $to, Region $region")

    return csrDetails
  }

  companion object {
    private val log = LoggerFactory.getLogger(CsrClient::class.java)
    private val CSR_DETAIL_DTO_LIST_TYPE = object : ParameterizedTypeReference<Collection<CsrDetailDto>>() {}
  }
}

data class CsrDetailDto @JsonCreator constructor(
  var shiftType: ShiftType,

  var detailStart: LocalDateTime,

  var detailEnd: LocalDateTime,

  var activity: String? = null,
)

data class CsrModifiedDetailDto @JsonCreator constructor(
  val id: Long? = null,

  val quantumId: String?,

  val shiftModified: LocalDateTime?,

  val shiftType: ShiftType,

  val detailStart: LocalDateTime,

  val detailEnd: LocalDateTime,

  val activity: String?,

  var actionType: DetailModificationType?,
)
