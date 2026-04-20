package uk.gov.justice.digital.hmpps.cmd.api.client

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.cmd.api.utils.RegionContext
import java.time.LocalDate

@Service
class CsrRegionSelectorService(private val csrDetailService: CsrDetailService) {
  fun getModified(region: Int): List<CsrModifiedDetailDto> = RegionContext.setRegion(region) {
    csrDetailService.getModified()
  }

  fun getStaffDetails(from: LocalDate, to: LocalDate, region: Int): Collection<CsrDetailDto> = RegionContext.setRegion(region) {
    csrDetailService.getStaffDetails(from, to)
  }

  fun deleteProcessed(ids: List<Long>, region: Int) {
    RegionContext.setRegion(region) {
      csrDetailService.deleteProcessed(ids)
    }
  }
}
