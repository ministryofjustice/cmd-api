package uk.gov.justice.digital.hmpps.cmd.api.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.web.context.annotation.RequestScope
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.cmd.api.utils.UserContext.getAuthentication
import uk.gov.justice.hmpps.kotlin.auth.authorisedWebClient
import java.time.Duration

@Configuration
class WebClientConfiguration(
  @Value("\${api.base.url.prison-api}") private val prisonApiRootUri: String,
  @Value("\${api.base.url.csr}") private val csrRootUri: String,
  @Value("\${api.timeout:90s}") val timeout: Duration,
) {

  @Bean
  @RequestScope
  fun prisonApiWebClient(
    clientRegistrationRepository: ClientRegistrationRepository,
    authorizedClientRepository: OAuth2AuthorizedClientRepository,
    builder: WebClient.Builder,
  ): WebClient = builder.authorisedWebClient(
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
    builder: WebClient.Builder,
  ): WebClient = builder.authorisedWebClient(
    authorizedClientManager = authorizedClientManagerRequestScope(clientRegistrationRepository, authorizedClientRepository),
    registrationId = "prison-api",
    url = csrRootUri,
    timeout = timeout,
  )

  @Bean
  fun prisonWebClientAppScope(
    @Qualifier("authorizedClientManager") authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder,
  ): WebClient = builder.authorisedWebClient(
    authorizedClientManager = authorizedClientManager,
    registrationId = "prison-api",
    url = prisonApiRootUri,
    timeout = timeout,
  )

  @Bean
  fun csrAPIWebClientAppScope(
    @Qualifier("authorizedClientManager") authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder,
  ): WebClient = builder.authorisedWebClient(
    authorizedClientManager = authorizedClientManager,
    registrationId = "prison-api",
    url = csrRootUri,
    timeout = timeout,
  )

  private fun authorizedClientManagerRequestScope(
    clientRegistrationRepository: ClientRegistrationRepository,
    authorizedClientRepository: OAuth2AuthorizedClientRepository,
  ): OAuth2AuthorizedClientManager {
    val defaultClientCredentialsTokenResponseClient = DefaultClientCredentialsTokenResponseClient()
    val authentication = getAuthentication()
    defaultClientCredentialsTokenResponseClient.setRequestEntityConverter { grantRequest: OAuth2ClientCredentialsGrantRequest? ->
      val converter = CustomOAuth2ClientCredentialsGrantRequestEntityConverter()
      val username = authentication.name
      converter.enhanceWithUsername(grantRequest, username)
    }
    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
      .clientCredentials { clientCredentialsGrantBuilder: OAuth2AuthorizedClientProviderBuilder.ClientCredentialsGrantBuilder ->
        clientCredentialsGrantBuilder.accessTokenResponseClient(
          defaultClientCredentialsTokenResponseClient,
        )
      }
      .build()
    val authorizedClientManager =
      DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository)
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
    return authorizedClientManager
  }
}
