package uk.gov.justice.digital.hmpps.cmd.api.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.endpoint.RestClientClientCredentialsTokenResponseClient
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.cmd.api.utils.UserContext.getAuthentication
import java.time.Duration
import kotlin.apply as kotlinApply

@Configuration
class WebClientConfiguration(
  @Value("\${api.base.url.prison-api}") private val prisonApiRootUri: String,
  @Value("\${api.base.url.csr}") private val csrRootUri: String,
  @Value("\${api.timeout:90s}") val timeout: Duration,
  @Value("\${csr.timeout}") private val csrApiTimeout: Duration,
) {

  @Bean
  @RequestScope
  fun prisonApiWebClient(
    clientRegistrationRepository: ClientRegistrationRepository,
    authorizedClientRepository: OAuth2AuthorizedClientRepository,
    builder: RestClient.Builder,
  ): RestClient = builder.authorisedRestClient(
    authorizedClientManager = authorizedClientManagerRequestScope(clientRegistrationRepository, authorizedClientRepository),
    registrationId = "prison-api",
    url = prisonApiRootUri,
    timeout = timeout,
  )

  @Bean
  @RequestScope
  fun csrApiWebClient(
    clientRegistrationRepository: ClientRegistrationRepository,
    authorizedClientRepository: OAuth2AuthorizedClientRepository,
    builder: RestClient.Builder,
  ): RestClient = builder.authorisedRestClient(
    authorizedClientManager = authorizedClientManagerRequestScope(clientRegistrationRepository, authorizedClientRepository),
    registrationId = "prison-api",
    url = csrRootUri,
    timeout = csrApiTimeout,
  )

  @Bean
  fun prisonWebClientAppScope(
    @Qualifier("authorizedClientManager") authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: RestClient.Builder,
  ): RestClient = builder.authorisedRestClient(
    authorizedClientManager = authorizedClientManager,
    registrationId = "prison-api",
    url = prisonApiRootUri,
    timeout = timeout,
  )

  @Bean
  fun csrAPIWebClientAppScope(
    @Qualifier("authorizedClientManager") authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: RestClient.Builder,
  ): RestClient = builder.authorisedRestClient(
    authorizedClientManager = authorizedClientManager,
    registrationId = "prison-api",
    url = csrRootUri,
    timeout = csrApiTimeout,
  )

  private fun authorizedClientManagerRequestScope(
    clientRegistrationRepository: ClientRegistrationRepository,
    authorizedClientRepository: OAuth2AuthorizedClientRepository,
  ): OAuth2AuthorizedClientManager {
    val responseClient = RestClientClientCredentialsTokenResponseClient().kotlinApply {
      addParametersConverter {
        LinkedMultiValueMap<String, String>().kotlinApply { this.add("username", getAuthentication().name) }
      }
    }
    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
      .clientCredentials { clientCredentialsGrantBuilder: OAuth2AuthorizedClientProviderBuilder.ClientCredentialsGrantBuilder ->
        clientCredentialsGrantBuilder.accessTokenResponseClient(
          responseClient,
        )
      }
      .build()
    val authorizedClientManager =
      DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository)
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
    return authorizedClientManager
  }
}

private const val DEFAULT_TIMEOUT_SECONDS: Long = 30

fun RestClient.Builder.authorisedRestClient(
  authorizedClientManager: OAuth2AuthorizedClientManager,
  registrationId: String,
  url: String,
  timeout: Duration = Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS),
): RestClient = baseUrl(url)
  .requestFactory(
    SimpleClientHttpRequestFactory().kotlinApply {
      this.setConnectTimeout(timeout)
      this.setReadTimeout(timeout)
    },
  )
  .requestInterceptor(
    OAuth2ClientHttpRequestInterceptor(authorizedClientManager).kotlinApply {
      setClientRegistrationIdResolver { registrationId }
    },
  )
  .build()
