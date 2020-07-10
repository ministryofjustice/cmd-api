package uk.gov.justice.digital.hmpps.cmd.api.service

import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.cmd.api.model.UserPreference
import uk.gov.justice.digital.hmpps.cmd.api.repository.UserPreferenceRepository
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

@ExtendWith(MockKExtension::class)
@DisplayName("Snooze Notification Preference Service tests")
internal class UserPreferenceServiceTest {
    private val repository: UserPreferenceRepository = mockk(relaxUnitFun = true)
    private val authenticationFacade: AuthenticationFacade = mockk(relaxUnitFun = true)
    private val now = LocalDate.now()
    private val clock = Clock.fixed(now.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
    private val service = UserPreferenceService(repository, clock, authenticationFacade)

    @BeforeEach
    fun resetAllMocks() {
        clearMocks(repository)
    }

    @Nested
    @DisplayName("Get Snooze Preference tests")
    inner class GetPreferenceTests {

        @Test
        fun `Should get preference with future date`() {
            val quantumId = "XYZ"
            val userPref = UserPreference(quantumId, now.plusDays(1))
            every { repository.findByQuantumId(any()) } returns userPref
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getUserSnoozePreference()

            verify { repository.findByQuantumId(quantumId) }
            confirmVerified(repository)

            assertThat(returnValue).isNotNull
            assertThat(returnValue.snoozeUntil).isEqualTo(userPref.snoozeUntil)
        }

        @Test
        fun `Should get preference with today's date`() {
            val quantumId = "XYZ"
            val userPref = UserPreference(quantumId, now)
            every { repository.findByQuantumId(any()) } returns userPref
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getUserSnoozePreference()

            verify { repository.findByQuantumId(quantumId) }
            confirmVerified(repository)

            assertThat(returnValue).isNotNull
            assertThat(returnValue.snoozeUntil).isEqualTo(userPref.snoozeUntil)
        }

        @Test
        fun `Should get preference with past date`() {
            val quantumId = "XYZ"
            every { repository.findByQuantumId(any()) } returns null
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getUserSnoozePreference()

            verify { repository.findByQuantumId(quantumId) }
            confirmVerified(repository)

            assertThat(returnValue).isNotNull
            assertThat(returnValue.snoozeUntil).isNull()
        }

        @Test
        fun `Should handle preference not found`() {
            val quantumId = "XYZ"
            every { repository.findByQuantumId(any()) } returns null
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getUserSnoozePreference()

            verify { repository.findByQuantumId(quantumId) }
            confirmVerified(repository)

            assertThat(returnValue).isNotNull
            assertThat(returnValue.snoozeUntil).isNull()
        }
    }

    @Nested
    @DisplayName("Update Snooze Preference tests")
    inner class UpdatePreferenceTests {

        @Test
        fun `Should updates an existing preference`() {
            val quantumId = "XYZ"
            val userPref = UserPreference(quantumId, now.plusDays(1))
            val newDate = now.plusDays(3)
            // This should equals() with the one created in the service code
            val userPrefToCompare = UserPreference(quantumId, newDate)

            every { repository.findByQuantumId(any()) } returns userPref
            every { authenticationFacade.currentUsername } returns quantumId
            every { repository.save(userPref) } returns userPref
            every { repository.save(userPrefToCompare) } returns userPrefToCompare

            service.createOrUpdateUserPreference(newDate)

            verify { repository.findByQuantumId(quantumId) }
            verify { repository.save(userPrefToCompare) }
            confirmVerified(repository)
        }

        @Test
        fun `Should update an existing preference that has an older date`() {
            val quantumId = "XYZ"
            val userPref = UserPreference(quantumId, now.plusDays(1))
            val newDate = now.minusDays(3)
            val userPrefToCompare = UserPreference(quantumId, newDate)
            every { repository.findByQuantumId(any()) } returns userPref
            every { authenticationFacade.currentUsername } returns quantumId
            every { repository.save(userPrefToCompare) } returns userPrefToCompare

            service.createOrUpdateUserPreference(newDate)

            verify { repository.findByQuantumId(quantumId) }
            verify { repository.save(userPrefToCompare) }
            confirmVerified(repository)
        }

        @Test
        fun `Should create a new preference with a future date`() {
            val quantumId = "XYZ"
            val newDate = now.plusDays(3)
            val userPrefToCompare = UserPreference(quantumId, newDate)
            every { repository.findByQuantumId(any()) } returns null
            every { authenticationFacade.currentUsername } returns quantumId
            every { repository.save(userPrefToCompare) } returns userPrefToCompare

            service.createOrUpdateUserPreference(newDate)

            verify { repository.findByQuantumId(quantumId) }
            verify { repository.save(userPrefToCompare) }
            confirmVerified(repository)
        }

        @Test
        fun `Should create a new preference with an older date`() {
            val quantumId = "XYZ"
            val newDate = now.minusDays(3)
            val userPrefToCompare = UserPreference(quantumId, newDate)

            every { repository.findByQuantumId(any()) } returns null
            every { authenticationFacade.currentUsername } returns quantumId
            every { repository.save(userPrefToCompare) } returns userPrefToCompare

            service.createOrUpdateUserPreference(newDate)

            verify { repository.findByQuantumId(quantumId) }
            verify { repository.save(userPrefToCompare) }
            confirmVerified(repository)
        }

    }


}