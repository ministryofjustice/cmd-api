package uk.gov.justice.digital.hmpps.cmd.api.utils.region

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

// 💩💩💩
@ConfigurationProperties(prefix = "csr")
@Configuration
class Regions {
    lateinit var regions: List<Region>
}

class Region {
    lateinit var name: String
}