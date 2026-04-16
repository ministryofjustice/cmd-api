package uk.gov.justice.digital.hmpps.cmd.api.utils.region

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "csr")
@Configuration
class Regions {
  lateinit var regions: List<Region>
}

data class Region(
  val name: String,
  val username: String,
  val password: String,
  val url: String,
  val driverClassName: String,
  val schema: String,
)
