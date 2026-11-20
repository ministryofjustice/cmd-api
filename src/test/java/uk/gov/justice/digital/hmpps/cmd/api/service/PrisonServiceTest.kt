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
import uk.gov.justice.digital.hmpps.cmd.api.client.ManageUsersApiClient
import uk.gov.justice.digital.hmpps.cmd.api.model.Prison
import uk.gov.justice.digital.hmpps.cmd.api.repository.PrisonRepository
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder

@DisplayName("Prison Service tests")
internal class PrisonServiceTest {
  private val prisonRepository: PrisonRepository = mock()
  private val manageUsersApiClient: ManageUsersApiClient = mock()
  private val authenticationFacade: HmppsAuthenticationHolder = mock()
  private val service = PrisonService(prisonRepository, manageUsersApiClient, authenticationFacade)

  @BeforeEach
  fun resetAllMocks() {
    reset(prisonRepository, manageUsersApiClient, authenticationFacade)
  }

  @Nested
  @DisplayName("Get Prison for User tests")
  inner class GetPrisonForUserTest {

    @Test
    fun `Should get Prison`() {
      val prisonId = "AKA"

      whenever(authenticationFacade.username).thenReturn("user1")
      whenever(manageUsersApiClient.getCurrentPrisonIdForUser("user1")).thenReturn(prisonId)
      whenever(prisonRepository.findByPrisonId(prisonId)).thenReturn(prison1)

      val pair = service.getPrisonAndRegionForUser()

      verify(prisonRepository).findByPrisonId(prisonId)
      verifyNoMoreInteractions(prisonRepository)

      assertThat(pair.first).isEqualTo(prison1.prisonId)
      assertThat(pair.second).isEqualTo(prison1.region)
    }

    @Test
    fun `Should get Prison not found`() {
      val prisonId = "AKA"

      whenever(authenticationFacade.username).thenReturn("user1")
      whenever(manageUsersApiClient.getCurrentPrisonIdForUser("user1")).thenReturn(prisonId)
      whenever(prisonRepository.findByPrisonId(prisonId)).thenReturn(null)

      val pair = service.getPrisonAndRegionForUser()

      verify(prisonRepository).findByPrisonId(prisonId)
      verifyNoMoreInteractions(prisonRepository)

      assertThat(pair.first).isEqualTo(prisonId)
      assertThat(pair.second).isNull()
    }
  }

  companion object {
    val prison1 = Prison("AKA", "Big plan", "Arkham Asylum", 5)
  }
}
