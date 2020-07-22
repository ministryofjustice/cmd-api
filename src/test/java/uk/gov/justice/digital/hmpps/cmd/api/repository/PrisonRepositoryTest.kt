package uk.gov.justice.digital.hmpps.cmd.api.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.Prison
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.repository.PrisonRepository
import java.time.LocalDate

@ActiveProfiles("test")
@DataJpaTest
class PrisonRepositoryTest(
        @Autowired val repository: PrisonRepository
) {

    private val now: LocalDate = LocalDate.now()

    @BeforeEach
    fun resetAllMocks() {
        repository.deleteAll()
    }

    @Nested
    @DisplayName("Get all Prisons")
    inner class GetPreferenceTests {

        @Test
        fun `Should return all prisons`() {

            val prison1 = Prison("AKA", "Big plan", "Arkham Asylum", 5)
            val prison2 = Prison("TPT", "Little plan", "The Pit", 3)

            val allPrisons1 = repository.findAll()
            assertThat(allPrisons1).isNullOrEmpty()

            repository.save(prison1)
            repository.save(prison2)

            val prisons2 = repository.findAll()
            assertThat(prisons2).isNotNull
            assertThat(prisons2).contains(prison1)
            assertThat(prisons2).contains(prison2)
        }
    }
}
