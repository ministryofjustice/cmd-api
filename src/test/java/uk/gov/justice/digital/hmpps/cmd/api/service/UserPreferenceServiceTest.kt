package uk.gov.justice.digital.hmpps.cmd.api.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.kotlin.any
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference
import uk.gov.justice.digital.hmpps.cmd.api.model.UserPreference
import uk.gov.justice.digital.hmpps.cmd.api.repository.UserPreferenceRepository
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.time.LocalDate

@DisplayName("Snooze Notification Preference Service tests")
internal class UserPreferenceServiceTest {
  private val repository: UserPreferenceRepository = mock()
  private val authenticationFacade: HmppsAuthenticationHolder = mock()
  private val now = LocalDate.now()
  private val service = UserPreferenceService(repository, authenticationFacade)

  @BeforeEach
  fun resetAllMocks() {
    reset(repository)
  }

  @Nested
  @DisplayName("Get or create preference tests")
  inner class GetOrCreatePreferenceTests {

    @Test
    fun `Should get preference with future date`() {
      val quantumId = "XYZ"
      val userPref = UserPreference(
        quantumId,
        now.plusDays(1),
        "Any Email",
        "Any Sms",
        CommunicationPreference.EMAIL,
      )
      whenever(repository.findByQuantumIdIgnoreCase(any())).thenReturn(userPref)
      whenever(authenticationFacade.username).thenReturn(quantumId)

      val returnValue = service.getOrCreateUserPreference()

      verify(repository).findByQuantumIdIgnoreCase(quantumId)
      verifyNoMoreInteractions(repository)

      assertThat(returnValue).isNotNull
      assertThat(returnValue.snoozeUntil).isEqualTo(userPref.snoozeUntil)
      assertThat(returnValue.email).isEqualTo(userPref.email)
      assertThat(returnValue.sms).isEqualTo(userPref.sms)
      assertThat(returnValue.commPref).isEqualTo(userPref.commPref)
    }

    @Test
    fun `Should get preference with today's date`() {
      val quantumId = "XYZ"
      val userPref = UserPreference(quantumId, now)
      whenever(repository.findByQuantumIdIgnoreCase(any())).thenReturn(userPref)
      whenever(authenticationFacade.username).thenReturn(quantumId)

      val returnValue = service.getOrCreateUserPreference()

      verify(repository).findByQuantumIdIgnoreCase(quantumId)
      verifyNoMoreInteractions(repository)

      assertThat(returnValue).isNotNull
      assertThat(returnValue.snoozeUntil).isEqualTo(userPref.snoozeUntil)
    }

    @Test
    fun `Should handle preference not found by creating a new one`() {
      val quantumId = "XYZ"
      whenever(repository.findByQuantumIdIgnoreCase(any())).thenReturn(null)
      whenever(repository.save(any<UserPreference>())).thenReturn(UserPreference(quantumId))
      whenever(authenticationFacade.username).thenReturn(quantumId)

      val returnValue = service.getOrCreateUserPreference()

      verify(repository).findByQuantumIdIgnoreCase(quantumId)
      verify(repository).save(any<UserPreference>())
      verifyNoMoreInteractions(repository)

      assertThat(returnValue).isNotNull
      assertThat(returnValue.snoozeUntil).isNull()
      assertThat(returnValue.email).isNull()
      assertThat(returnValue.sms).isNull()
      assertThat(returnValue.commPref).isEqualTo(CommunicationPreference.NONE)
    }

    @Test
    fun `Should get preference case insensitive`() {
      val quantumId = "XyZ"
      val userPref = UserPreference(quantumId.lowercase(), now)
      whenever(repository.findByQuantumIdIgnoreCase(any())).thenReturn(userPref)
      whenever(authenticationFacade.username).thenReturn(quantumId)

      val returnValue = service.getOrCreateUserPreference()

      verify(repository).findByQuantumIdIgnoreCase(quantumId)
      verifyNoMoreInteractions(repository)

      assertThat(returnValue).isNotNull
      assertThat(returnValue.snoozeUntil).isEqualTo(userPref.snoozeUntil)
    }
  }

  @Nested
  @DisplayName("Get Preference tests")
  inner class GetPreferenceTests {
    @Test
    fun `Should handle preference not found by returning null`() {
      val quantumId = "XYZ"
      whenever(repository.findByQuantumIdIgnoreCase(any())).thenReturn(null)

      val returnValue = service.getUserPreference(quantumId)

      verify(repository).findByQuantumIdIgnoreCase(quantumId)
      verifyNoMoreInteractions(repository)

      assertThat(returnValue).isNull()
    }
  }

  @Nested
  @DisplayName("Update Snooze Preference tests")
  inner class UpdateSnoozePreferenceTests {

    @Test
    fun `Should updates an existing preference`() {
      val quantumId = "XYZ"
      val userPref = UserPreference(quantumId, now.plusDays(1))
      val newDate = now.plusDays(3)

      whenever(repository.findByQuantumIdIgnoreCase(any())).thenReturn(userPref)
      whenever(repository.save(userPref)).thenReturn(userPref)
      whenever(authenticationFacade.username).thenReturn(quantumId)

      service.updateSnoozePreference(newDate)

      verify(repository).findByQuantumIdIgnoreCase(quantumId)
      verify(repository).save(userPref)
      verifyNoMoreInteractions(repository)
    }

    @Test
    fun `Should update an existing preference that has an older date`() {
      val quantumId = "XYZ"
      val userPref = UserPreference(quantumId, now.plusDays(1))
      val newDate = now.minusDays(3)
      whenever(repository.findByQuantumIdIgnoreCase(any())).thenReturn(userPref)
      whenever(repository.save(userPref)).thenReturn(userPref)
      whenever(authenticationFacade.username).thenReturn(quantumId)

      service.updateSnoozePreference(newDate)

      verify(repository).findByQuantumIdIgnoreCase(quantumId)
      verify(repository).save(userPref)
      verifyNoMoreInteractions(repository)
    }

    @Test
    fun `Should create a new preference with a future date`() {
      val quantumId = "XYZ"
      val newDate = now.plusDays(3)
      whenever(repository.findByQuantumIdIgnoreCase(any())).thenReturn(null)
      whenever(repository.save(any<UserPreference>())).thenReturn(UserPreference(quantumId))
      whenever(authenticationFacade.username).thenReturn(quantumId)

      service.updateSnoozePreference(newDate)

      verify(repository).findByQuantumIdIgnoreCase(quantumId)
      verify(repository, times(2)).save(any<UserPreference>())
      verifyNoMoreInteractions(repository)
    }

    @Test
    fun `Should create a new preference with an older date`() {
      val quantumId = "XYZ"
      val newDate = now.minusDays(3)

      whenever(repository.findByQuantumIdIgnoreCase(any())).thenReturn(null)
      whenever(repository.save(any<UserPreference>())).thenReturn(UserPreference(quantumId))
      whenever(authenticationFacade.username).thenReturn(quantumId)

      service.updateSnoozePreference(newDate)

      verify(repository).findByQuantumIdIgnoreCase(quantumId)
      verify(repository, times(2)).save(any<UserPreference>())
      verifyNoMoreInteractions(repository)
    }
  }

  @Nested
  @DisplayName("Update Notification Preference tests")
  inner class UpdateNotificationPreferenceTests {

    @Test
    fun `Should updates an existing preference`() {
      val quantumId = "XYZ"
      val userPref = UserPreference(
        quantumId,
        now.plusDays(1),
        "Any Email",
        "Any Sms",
        CommunicationPreference.EMAIL,
      )

      whenever(repository.findByQuantumIdIgnoreCase(any())).thenReturn(userPref)
      whenever(repository.save(userPref)).thenReturn(userPref)
      whenever(authenticationFacade.username).thenReturn(quantumId)

      service.updateNotificationDetails("new Email", "new Sms", CommunicationPreference.EMAIL)

      verify(repository).findByQuantumIdIgnoreCase(quantumId)
      verify(repository).save(userPref)
      verifyNoMoreInteractions(repository)
    }

    @Test
    fun `Should create a new preference on update when one doesn't already exist`() {
      val quantumId = "XYZ"
      whenever(repository.findByQuantumIdIgnoreCase(any())).thenReturn(null)
      whenever(repository.save(any<UserPreference>())).thenReturn(UserPreference(quantumId))
      whenever(authenticationFacade.username).thenReturn(quantumId)

      service.updateNotificationDetails("new Email", "new Sms", CommunicationPreference.EMAIL)

      verify(repository).findByQuantumIdIgnoreCase(quantumId)
      verify(repository, times(2)).save(any<UserPreference>())
      verifyNoMoreInteractions(repository)
    }
  }
}
