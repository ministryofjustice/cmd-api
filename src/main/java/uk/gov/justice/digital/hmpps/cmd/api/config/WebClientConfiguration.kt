package uk.gov.justice.digital.hmpps.cmd.api.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor
import org.springframework.web.client.RestClient
import org.springframework.web.context.annotation.RequestScope
import java.time.Duration
import kotlin.apply as kotlinApply

@Configuration
class WebClientConfiguration(
  @Value($$"${api.base.url.manage-users-api}") private val manageUsersApiRootUri: String,
  @Value($$"${api.timeout:90s}") val timeout: Duration,
) {
  @Bean
  @RequestScope
  fun manageUsersApiWebClient(
    authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: RestClient.Builder,
  ): RestClient = builder.authorisedRestClient(
    authorizedClientManager = authorizedClientManager,
    registrationId = "manage-users-api",
    url = manageUsersApiRootUri,
    timeout = timeout,
  )
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
