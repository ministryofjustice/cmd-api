package uk.gov.justice.digital.hmpps.cmd.api.model

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.Prison

class PrisonTest {

    @Test
    fun `Should return a valid prison`() {

        val prisonId = "AKA"
        val csrPlanUnit = "We have a plan"
        val prisonName = "Arkham Asylum"
        val region = 5

        val prison = Prison(
                prisonId,
                csrPlanUnit,
                prisonName,
                region
        )

        Assertions.assertThat(prison.prisonId).isEqualTo(prisonId)
        Assertions.assertThat(prison.csrPlanUnit).isEqualTo(csrPlanUnit)
        Assertions.assertThat(prison.prisonName).isEqualTo(prisonName)
        Assertions.assertThat(prison.region).isEqualTo(region)
    }
}