package uk.gov.justice.digital.hmpps.cmd.api.service

import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.Prison
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.repository.PrisonRepository
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.service.PrisonService

@ExtendWith(MockKExtension::class)
@DisplayName("Prison Service tests")
internal class PrisonServiceTest {
    private val prisonRepository: PrisonRepository = mockk(relaxUnitFun = true)
    private val service = PrisonService(prisonRepository)

    @BeforeEach
    fun resetAllMocks() {
        clearMocks(prisonRepository)
    }

    @Nested
    @DisplayName("Get Prisons tests")
    inner class GetPrisonsTest {

        @Test
        fun `Should get Prisons`() {
            val prisonsStub = getValidPrisons()
            every { prisonRepository.findAll()} returns prisonsStub
            assertThat(service.getAllPrisons()).hasSize(2)
            assertThat(service.getAllPrisons()).contains(prison1)
        }
    }


    companion object {
        val prison1 = Prison("AKA", "Big plan", "Arkham Asylum", 5)
        private val prison2 = Prison("TPT", "Little plan", "The Pit", 3)

        fun getValidPrison(): Prison {
            return prison1
        }

        fun getValidPrisons(): Iterable<Prison> {
            return listOf(prison1, prison2).asIterable()
        }
    }
}