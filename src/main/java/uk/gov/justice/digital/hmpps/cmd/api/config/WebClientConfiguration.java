package uk.gov.justice.digital.hmpps.cmd.api.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import uk.gov.justice.digital.hmpps.cmd.api.utils.UserContext;

@Configuration
public class WebClientConfiguration {

    private final String elite2ApiRootUri;
    private final String csrRootUri;

    public WebClientConfiguration(
            @Value("${elite2api.endpoint.url}") final String eliteHealthRootUri,
            @Value("${csr.endpoint.url}") final String csrRootUri) {

        this.elite2ApiRootUri = eliteHealthRootUri;
        this.csrRootUri = csrRootUri;
    }

    @Bean
    @RequestScope
    public WebClient elite2ApiWebClient(final ClientRegistrationRepository clientRegistrationRepository,
                                     final OAuth2AuthorizedClientRepository authorizedClientRepository,
                                     final WebClient.Builder builder) {
        
        return getOAuthWebClient(authorizedClientManager(clientRegistrationRepository, authorizedClientRepository), builder, elite2ApiRootUri);
    }

    @Bean
    @RequestScope
    public WebClient csrApiWebClient(final ClientRegistrationRepository clientRegistrationRepository,
                                       final OAuth2AuthorizedClientRepository authorizedClientRepository,
                                       final WebClient.Builder builder) {
        return getOAuthWebClient(authorizedClientManager(clientRegistrationRepository, authorizedClientRepository), builder, csrRootUri);
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManagerAppScope(final ClientRegistrationRepository clientRegistrationRepository,
                                                                         final OAuth2AuthorizedClientService oAuth2AuthorizedClientService) {

        final var authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build();
        final var authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, oAuth2AuthorizedClientService);

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        return authorizedClientManager;
    }

    @Bean
    public WebClient elite2WebClientAppScope(@Qualifier(value = "authorizedClientManagerAppScope") final OAuth2AuthorizedClientManager authorizedClientManager, final WebClient.Builder builder) {
         return getOAuthWebClient(authorizedClientManager, builder, elite2ApiRootUri);
    }

    @Bean
    public WebClient csrAPIWebClientAppScope(@Qualifier(value = "authorizedClientManagerAppScope") final OAuth2AuthorizedClientManager authorizedClientManager, final WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create()
                .tcpConfiguration(tcpClient -> {
                    tcpClient = tcpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3_600_000);
                    return tcpClient;
                });
        builder.clientConnector(new ReactorClientHttpConnector(httpClient));
        return getOAuthWebClient(authorizedClientManager, builder, csrRootUri);
    }

    private WebClient getOAuthWebClient(final OAuth2AuthorizedClientManager authorizedClientManager, final WebClient.Builder builder, final String rootUri) {
        final var oauth2Client = new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth2Client.setDefaultClientRegistrationId("elite2-api");

        return builder.baseUrl(rootUri)
                .apply(oauth2Client.oauth2Configuration())
                .build();
    }

    private OAuth2AuthorizedClientManager authorizedClientManager(final ClientRegistrationRepository clientRegistrationRepository,
                                                                  final OAuth2AuthorizedClientRepository authorizedClientRepository) {

        final var defaultClientCredentialsTokenResponseClient = new DefaultClientCredentialsTokenResponseClient();
        final var authentication = UserContext.INSTANCE.getAuthentication();

        defaultClientCredentialsTokenResponseClient.setRequestEntityConverter(grantRequest -> {
            final var converter = new CustomOAuth2ClientCredentialsGrantRequestEntityConverter();
            final var username = authentication.getName();
            return converter.enhanceWithUsername(grantRequest, username);
        });

        final var authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials(clientCredentialsGrantBuilder -> clientCredentialsGrantBuilder.accessTokenResponseClient(defaultClientCredentialsTokenResponseClient))
                        .build();

        final var authorizedClientManager = new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository);

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        return authorizedClientManager;
    }
}
