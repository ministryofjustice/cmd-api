package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer
import org.springframework.cache.annotation.CachingConfigurer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
@EnableCaching(proxyTargetClass = true)
class CacheConfig : CachingConfigurer {
  @Bean
  fun cacheManagerCustomizer(): CacheManagerCustomizer<CaffeineCacheManager> = CacheManagerCustomizer {
    it.registerCustomCache("jwks", Caffeine.newBuilder().maximumSize(1).build())
    it.registerCustomCache(
      "userDetails",
      Caffeine.newBuilder()
        .maximumSize(2000)
        .expireAfterWrite(Duration.ofMinutes(5))
        .recordStats()
        .build(),
    )
  }
}
