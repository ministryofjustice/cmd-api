package uk.gov.justice.digital.hmpps.cmd.api.config

import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CsrConfiguration::class)
class CsrRegionDataSourceConfiguration(
  @Autowired private val csrConfiguration: CsrConfiguration,
) {

  @Bean(defaultCandidate = false)
  fun regionDataSource(@Value($$"${csr.flyway.enabled:false}") flywayEnabled: Boolean): DataSource = HikariDataSource().also {
    it.driverClassName = csrConfiguration.driverClassName
    it.jdbcUrl = csrConfiguration.url
    it.username = csrConfiguration.username
    it.password = csrConfiguration.password
    it.minimumIdle = 5
    it.maximumPoolSize = 30
  }.also {
    if (flywayEnabled) {
      migrateRegionDb(it)
    }
  }

  @Bean(defaultCandidate = false)
  fun regionJdbcTemplate(@Qualifier("regionDataSource") regionDataSource: DataSource): NamedParameterJdbcTemplate = NamedParameterJdbcTemplate(regionDataSource)

  @Bean(defaultCandidate = false)
  fun regionTransactionManager(@Qualifier("regionDataSource") regionDataSource: DataSource): PlatformTransactionManager = DataSourceTransactionManager(regionDataSource)

  private fun migrateRegionDb(regionDataSource: HikariDataSource) {
    Flyway.configure()
      .dataSource(regionDataSource)
      .locations("classpath:csr/migration/h2")
      .createSchemas(false)
      .schemas("PUBLIC", "R2")
      .load()
      .migrate()
  }
}

@ConfigurationProperties(prefix = "csr")
data class CsrConfiguration(
  val username: String,
  val password: String,
  val url: String,
  val driverClassName: String,
  val regions: List<Region>,
)

data class Region(
  val name: Int,
  val schema: String,
)
