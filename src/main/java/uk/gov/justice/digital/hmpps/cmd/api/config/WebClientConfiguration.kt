package uk.gov.justice.digital.hmpps.cmd.api.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.context.annotation.RequestScope
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import uk.gov.justice.digital.hmpps.cmd.api.utils.UserContext.getAuthentication
import java.time.Duration
import kotlin.apply as kotlinApply

@Configuration
class WebClientConfiguration(
  @Value("\${elite2api.endpoint.url}") private val elite2ApiRootUri: String,
  @Value("\${csr.endpoint.url}") private val csrRootUri: String,
  @Value("\${api.timeout:90s}") val timeout: Duration,
) {

  @Bean
  @RequestScope
  fun elite2ApiWebClient(
    clientRegistrationRepository: ClientRegistrationRepository,
    authorizedClientRepository: OAuth2AuthorizedClientRepository,
    builder: WebClient.Builder,
  ): WebClient = builder.authorisedWebClient(
    authorizedClientManagerRequestScope(clientRegistrationRepository, authorizedClientRepository),
    registrationId = "elite2-api",
    url = elite2ApiRootUri,
    timeout,
  )

  @Bean
  @RequestScope
  fun csrApiWebClient(
    clientRegistrationRepository: ClientRegistrationRepository,
    authorizedClientRepository: OAuth2AuthorizedClientRepository,
    builder: WebClient.Builder,
  ): WebClient = builder.authorisedWebClient(
    authorizedClientManagerRequestScope(clientRegistrationRepository, authorizedClientRepository),
    registrationId = "elite2-api",
    url = csrRootUri,
    timeout,
  )

  @Bean
  fun elite2WebClientAppScope(
    @Qualifier("authorizedClientManagerAppScope") authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder,
  ): WebClient =
    builder.authorisedWebClient(authorizedClientManager, registrationId = "elite2-api", url = elite2ApiRootUri, timeout)

  @Bean
  fun csrAPIWebClientAppScope(
    @Qualifier("authorizedClientManagerAppScope") authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder,
  ): WebClient =
    builder.authorisedWebClient(authorizedClientManager, registrationId = "elite2-api", url = csrRootUri, timeout)

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

  @Bean
  fun authorizedClientManagerAppScope(
    clientRegistrationRepository: ClientRegistrationRepository,
    oAuth2AuthorizedClientService: OAuth2AuthorizedClientService,
  ): OAuth2AuthorizedClientManager = AuthorizedClientServiceOAuth2AuthorizedClientManager(
    clientRegistrationRepository,
    oAuth2AuthorizedClientService,
  ).kotlinApply {
    setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build())
  }
}

fun WebClient.Builder.authorisedWebClient(authorizedClientManager: OAuth2AuthorizedClientManager, registrationId: String, url: String, timeout: Duration): WebClient =
  baseUrl(url)
    .clientConnector(ReactorClientHttpConnector(HttpClient.create().responseTimeout(timeout)))
    .filter(
      ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager).kotlinApply {
        setDefaultClientRegistrationId(registrationId)
      },
    )
    .build()
