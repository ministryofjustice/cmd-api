package uk.gov.justice.digital.hmpps.cmd.api.client

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.cmd.api.config.CsrConfiguration
import uk.gov.justice.digital.hmpps.cmd.api.config.Region
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder

@DisplayName("Csr Client tests")
internal class CsrClientTest {
  private val csrRegionSelectorService: CsrRegionSelectorService = mock()
  private val authenticationFacade: HmppsAuthenticationHolder = mock()
  private val service = CsrClient(
    csrRegionSelectorService,
    authenticationFacade,
    CsrConfiguration(
      username = "user",
      password = "pass",
      url = "url",
      driverClassName = "driver",
      regions = listOf(
        Region(1, "REGION"),
      ),
    ),
  )

  @BeforeEach
  fun resetMocks() {
    reset(csrRegionSelectorService, authenticationFacade)
  }

  @Nested
  @DisplayName("Delete processed tests")
  inner class DeleteProcessedTests {
    @Test
    fun `Should split large id array into several SQL calls`() {
      val ids = List(102) { it + 1L }
      val chunk1 = List(100) { it + 1L }
      val chunk2 = List(2) { it + 101L }

      service.deleteProcessed(1, ids)

      verify(csrRegionSelectorService).deleteProcessed(chunk1, 1)
      verify(csrRegionSelectorService).deleteProcessed(chunk2, 1)
    }

    @Test
    fun `Should do small id array in one go`() {
      val ids = List(10) { it + 1L }
      service.deleteProcessed(1, ids)

      verify(csrRegionSelectorService).deleteProcessed(ids, 1)
    }

    @Test
    fun `Should continue after failure`() {
      val ids = List(102) { it + 1L }
      val chunk1 = List(100) { it + 1L }
      val chunk2 = List(2) { it + 101L }
      whenever(csrRegionSelectorService.deleteProcessed(chunk1, 1)).thenThrow(RuntimeException("test"))

      service.deleteProcessed(1, ids)

      verify(csrRegionSelectorService).deleteProcessed(chunk1, 1)
      verify(csrRegionSelectorService).deleteProcessed(chunk2, 1)
    }
  }
}
