package uk.gov.justice.digital.hmpps.cmd.api.client

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.cmd.api.config.CsrConfiguration
import uk.gov.justice.digital.hmpps.cmd.api.repository.CsrSqlRepository
import java.time.LocalDate

@Transactional(transactionManager = "regionTransactionManager", readOnly = true)
@Service
class CsrRegionSelectorService(
  private val csrDetailService: CsrDetailService,
  private val csrSqlRepository: CsrSqlRepository,
  csrConfiguration: CsrConfiguration,
) {
  private val regionMap = csrConfiguration.regions.associate { it.name to it.schema }

  fun getModified(region: Int): List<CsrModifiedDetailDto> = setDatabaseSchema(region) {
    csrDetailService.getModified()
  }

  fun getStaffDetails(from: LocalDate, to: LocalDate, region: Int): Collection<CsrDetailDto> = setDatabaseSchema(region) {
    csrDetailService.getStaffDetails(from, to)
  }

  @Transactional(transactionManager = "regionTransactionManager")
  fun deleteProcessed(ids: List<Long>, region: Int) {
    setDatabaseSchema(region) {
      csrDetailService.deleteProcessed(ids)
    }
  }

  private fun <T> setDatabaseSchema(region: Int, function: () -> T): T {
    csrSqlRepository.setSchema(regionMap[region]!!)
    return function()
  }
}
