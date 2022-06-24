package uk.gov.justice.digital.hmpps.cmd.api.service

import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
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
  @DisplayName("Get Prison for User tests")
  inner class GetPrisonForUserTest {

    @Test
    fun `Should get Prison`() {
      val prisonId = "AKA"

      every { elite2Client.getCurrentPrisonIdForUser() } returns prisonId
      every { prisonRepository.findByPrisonId(prisonId) } returns prison1

      val prison = service.getPrisonForUser()

      verify { prisonRepository.findByPrisonId(prisonId) }
      confirmVerified(prisonRepository)

      assertThat(prison).isEqualTo(prison1)
    }

    @Test
    fun `Should get Prison not found`() {
      val prisonId = "AKA"

      every { elite2Client.getCurrentPrisonIdForUser() } returns prisonId
      every { prisonRepository.findByPrisonId(prisonId) } returns null

      val prison = service.getPrisonForUser()

      verify { prisonRepository.findByPrisonId(prisonId) }
      confirmVerified(prisonRepository)

      assertThat(prison).isEqualTo(null)
    }
  }

  companion object {
    val prison1 = Prison("AKA", "Big plan", "Arkham Asylum", 5)
  }
}
