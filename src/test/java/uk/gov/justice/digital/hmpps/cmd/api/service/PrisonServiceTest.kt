package uk.gov.justice.digital.hmpps.cmd.api.service

import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.cmd.api.client.Elite2ApiClient
import uk.gov.justice.digital.hmpps.cmd.api.model.Prison
import uk.gov.justice.digital.hmpps.cmd.api.repository.PrisonRepository

@ExtendWith(MockKExtension::class)
@DisplayName("Prison Service tests")
internal class PrisonServiceTest {
    private val prisonRepository: PrisonRepository = mockk(relaxUnitFun = true)
    private val elite2Client: Elite2ApiClient = mockk(relaxUnitFun = true)
    private val service = PrisonService(prisonRepository, elite2Client)

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

            val prisons = service.getAllPrisons()

            verify { prisonRepository.findAll() }
            confirmVerified(prisonRepository)

            assertThat(prisons).hasSize(2)
            assertThat(prisons).contains(prison1)
            assertThat(prisons).contains(prison2)

        }
    }

    @Nested
    @DisplayName("Get Prison for User tests")
    inner class GetPrisonForUserTest {

        @Test
        fun `Should get Prison`() {
            val prisonId = "AKA"

            every { prisonRepository.findByPrisonId(prisonId)} returns prison1

            val prison = service.getPrisonForUser()

            verify { prisonRepository.findByPrisonId(prisonId) }
            confirmVerified(prisonRepository)

            assertThat(prison).isEqualTo(prison1)
        }

        @Test
        fun `Should get Prison not found`() {
            val prisonId = "AKA"

            every { elite2Client.getCurrentPrisonIdForUser()} returns prisonId
            every { prisonRepository.findByPrisonId(prisonId)} returns null

            val prison = service.getPrisonForUser()

            verify { prisonRepository.findByPrisonId(prisonId) }
            confirmVerified(prisonRepository)

            assertThat(prison).isEqualTo(null)
        }

        @Test
        fun `Should not get Prison magic number `() {
            val prisonId = "AKA"

            every { elite2Client.getCurrentPrisonIdForUser()} returns prisonId
            every { prisonRepository.findByPrisonId(prisonId)} returns null

            val prison = service.getPrisonForUser()

            verify { prisonRepository.findByPrisonId(prisonId) }
            confirmVerified(prisonRepository)

            assertThat(prison).isEqualTo(null)
        }
    }


    companion object {
        val prison1 = Prison("AKA", "Big plan", "Arkham Asylum", 5)
        val prison2 = Prison("TPT", "Little plan", "The Pit", 3)

        fun getValidPrisons(): Collection<Prison> {
            return listOf(prison1, prison2)
        }
    }
}