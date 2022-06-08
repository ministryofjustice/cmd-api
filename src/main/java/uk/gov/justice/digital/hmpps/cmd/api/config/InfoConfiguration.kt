package uk.gov.justice.digital.hmpps.cmd.api.config

import com.github.benmanes.caffeine.cache.Cache
import org.springframework.boot.actuate.info.Info.Builder
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Component

/**
 * Adds some basic stats for the /user/details cache to /info
 */
@Component
class InfoConfiguration(private val cacheManager: CacheManager) : InfoContributor {

  override fun contribute(builder: Builder) {
    cacheManager.cacheNames.forEach { name ->
      val nativeCache = cacheManager.getCache(name)?.nativeCache
      if (nativeCache is Cache<*, *>) {
        val stats = nativeCache.stats()
        builder.withDetail("cache-$name", stats.toString())
      }
    }
  }
}
