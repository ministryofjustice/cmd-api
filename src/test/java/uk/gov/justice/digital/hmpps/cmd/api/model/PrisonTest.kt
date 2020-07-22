package uk.gov.justice.digital.hmpps.cmd.api.model

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.Prison

class PrisonTest {

    @Test
    fun `Should return a valid prison`() {

        val id = 1
        val name = "Arkham Asylum"
        val description = "High security"
        val apiUrl = "arkham.goth.us"
        val regionId = null

        val prison = Prison(
                id,
                name,
                description,
                apiUrl,
                regionId
        )

        Assertions.assertThat(prison.id).isEqualTo(id)
        Assertions.assertThat(prison.name).isEqualTo(name)
        Assertions.assertThat(prison.description).isEqualTo(description)
        Assertions.assertThat(prison.apiUrl).isEqualTo(apiUrl)
        Assertions.assertThat(prison.regionId).isEqualTo(regionId)
    }
}