package uk.gov.justice.digital.hmpps.cmd.api.config

import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.convert.converter.Converter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class ResourceServerConfiguration : WebSecurityConfigurerAdapter() {
  @Throws(Exception::class)
  public override fun configure(http: HttpSecurity) {
    http.headers().frameOptions().sameOrigin().and()
      .sessionManagement()
      .sessionCreationPolicy(STATELESS) // Can't have CSRF protection as requires session
      .and().csrf().disable()
      .authorizeRequests({ auth ->
        auth
          .antMatchers(
            "/webjars/**",
            "/favicon.ico",
            "/health/**",
            "/info",
            "/h2-console/**",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/swagger-resources",
            "/swagger-resources/configuration/ui",
            "/swagger-resources/configuration/security"
          )
          .permitAll()
          .anyRequest()
          .authenticated()
      })
      .oauth2ResourceServer().jwt().jwtAuthenticationConverter(AuthAwareTokenConverter())
  }

  @Bean
  fun lockProvider(jdbcTemplate: JdbcTemplate): LockProvider =
    JdbcTemplateLockProvider(
      JdbcTemplateLockProvider.Configuration.builder()
        .withJdbcTemplate(jdbcTemplate)
        .usingDbTime()
        .build()
    )

  class AuthAwareTokenConverter : Converter<Jwt, AbstractAuthenticationToken> {
    private val jwtGrantedAuthoritiesConverter: Converter<Jwt, Collection<GrantedAuthority>> = JwtGrantedAuthoritiesConverter()

    override fun convert(jwt: Jwt): AbstractAuthenticationToken = AuthAwareAuthenticationToken(jwt, extractAuthorities(jwt))

    @Suppress("UNCHECKED_CAST", "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
      val authorities = jwtGrantedAuthoritiesConverter.convert(jwt).toMutableSet()
      if (jwt.claims.containsKey("authorities")) {
        authorities.addAll(
          (jwt.claims["authorities"] as Collection<String?>)
            .map { SimpleGrantedAuthority(it) }.toSet()
        )
      }
      return authorities.toSet()
    }
  }

  internal class AuthAwareAuthenticationToken(jwt: Jwt, authorities: Collection<GrantedAuthority>) : JwtAuthenticationToken(jwt, authorities) {
    override fun getPrincipal(): Any = name
  }
}

@Configuration
@Profile("!test") // prevent scheduler running during integration tests
@EnableScheduling
@EnableSchedulerLock(defaultLockAtLeastFor = "PT1M", defaultLockAtMostFor = "PT1H")
class SpringSchedulingConfig
