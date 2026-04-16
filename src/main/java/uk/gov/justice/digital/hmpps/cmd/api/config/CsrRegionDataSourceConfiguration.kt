package uk.gov.justice.digital.hmpps.cmd.api.config

import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.transaction.PlatformTransactionManager
import uk.gov.justice.digital.hmpps.cmd.api.utils.RegionContext
import javax.sql.DataSource

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
class CsrRegionDataSourceConfiguration(
  @Autowired private val regionData: Regions,
) {

  @Bean(defaultCandidate = false)
  fun regionDataSource(@Value($$"${csr.flyway.enabled:false}") flywayEnabled: Boolean): DataSource = RegionAwareRoutingSource().apply {
    // Read the region array in from application properties and construct data sources mapped to region names
    setTargetDataSources(
      regionData.regions.associate { it.name to regionDataSource(it) },
    )
  }.also {
    if (flywayEnabled) {
      migrateRegionDb(regionDataSource(regionData.regions.first()))
    }
  }

  @Bean(defaultCandidate = false)
  fun regionJdbcTemplate(@Qualifier("regionDataSource") regionDataSource: DataSource): NamedParameterJdbcTemplate = NamedParameterJdbcTemplate(regionDataSource)

  @Bean(defaultCandidate = false)
  fun regionTransactionManager(@Qualifier("regionDataSource") regionDataSource: DataSource): PlatformTransactionManager = DataSourceTransactionManager(regionDataSource)

  private fun regionDataSource(region: Region): HikariDataSource = HikariDataSource().also {
    it.driverClassName = region.driverClassName
    it.jdbcUrl = region.url
    it.username = region.username
    it.password = region.password
    it.schema = region.schema
    it.setReadOnly(true)
    it.setMaximumPoolSize(35)
  }
  private fun migrateRegionDb(regionDataSource: HikariDataSource) {
    regionDataSource.setReadOnly(false)
    Flyway.configure()
      .dataSource(regionDataSource)
      .locations("classpath:csr/migration/h2")
      .createSchemas(false)
      .schemas("PUBLIC", "R2")
      .load()
      .migrate()
  }
}

class RegionAwareRoutingSource : AbstractRoutingDataSource() {
  override fun determineCurrentLookupKey(): Any = RegionContext.getRegion().toString()
}
