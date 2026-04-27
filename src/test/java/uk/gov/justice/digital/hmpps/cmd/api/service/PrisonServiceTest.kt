package uk.gov.justice.digital.hmpps.cmd.api.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.cmd.api.client.PrisonApiClient
import uk.gov.justice.digital.hmpps.cmd.api.model.Prison
import uk.gov.justice.digital.hmpps.cmd.api.repository.PrisonRepository

@DisplayName("Prison Service tests")
internal class PrisonServiceTest {
  private val prisonRepository: PrisonRepository = mock()
  private val prisonApiClient: PrisonApiClient = mock()
  private val service = PrisonService(prisonRepository, prisonApiClient)

  @BeforeEach
  fun resetAllMocks() {
    reset(prisonRepository)
  }

  @Nested
  @DisplayName("Get Prison for User tests")
  inner class GetPrisonForUserTest {

    @Test
    fun `Should get Prison`() {
      val prisonId = "AKA"

      whenever(prisonApiClient.getCurrentPrisonIdForUser()).thenReturn(prisonId)
      whenever(prisonRepository.findByPrisonId(prisonId)).thenReturn(prison1)

      val prison = service.getPrisonForUser()

      verify(prisonRepository).findByPrisonId(prisonId)
      verifyNoMoreInteractions(prisonRepository)

      assertThat(prison).isEqualTo(prison1)
    }

    @Test
    fun `Should get Prison not found`() {
      val prisonId = "AKA"

      whenever(prisonApiClient.getCurrentPrisonIdForUser()).thenReturn(prisonId)
      whenever(prisonRepository.findByPrisonId(prisonId)).thenReturn(null)

      val prison = service.getPrisonForUser()

      verify(prisonRepository).findByPrisonId(prisonId)
      verifyNoMoreInteractions(prisonRepository)

      assertThat(prison).isEqualTo(null)
    }
  }

  companion object {
    val prison1 = Prison("AKA", "Big plan", "Arkham Asylum", 5)
  }
}
