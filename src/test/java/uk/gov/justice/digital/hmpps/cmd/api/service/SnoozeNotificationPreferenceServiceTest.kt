package uk.gov.justice.digital.hmpps.cmd.api.service

import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.SnoozePreference
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.repository.SnoozePreferenceRepository
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.service.SnoozeNotificationPreferenceService
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

@ExtendWith(MockKExtension::class)
@DisplayName("Snooze Notification Preference Service tests")
internal class SnoozeNotificationPreferenceServiceTest {
    private val repository: SnoozePreferenceRepository = mockk(relaxUnitFun = true)
    private val authenticationFacade: AuthenticationFacade = mockk(relaxUnitFun = true)
    private val now = LocalDate.now()
    private val clock = Clock.fixed(now.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
    private val service = SnoozeNotificationPreferenceService(repository, clock, authenticationFacade)

    @BeforeEach
    fun resetAllMocks() {
        clearMocks(repository)
    }

    @Nested
    @DisplayName("Get Snooze Preference tests")
    inner class GetPreferenceTests {

        @Test
        fun `Preference with future date exists in the database`() {
            val quantumId = "XYZ"
            val snoozePref = SnoozePreference(quantumId, now.plusDays(1))
            every { repository.findByQuantumIdAndSnoozeGreaterThanEqual(any(), eq(now)) } returns snoozePref
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getSnoozePreference()

            verify { repository.findByQuantumIdAndSnoozeGreaterThanEqual(quantumId, now) }
            confirmVerified(repository)

            assertThat(returnValue).isNotNull
            assertThat(returnValue.snooze).isEqualTo(snoozePref.snooze)
        }

        @Test
        fun `Preference with today's date exists in the database`() {
            val quantumId = "XYZ"
            val snoozePref = SnoozePreference(quantumId, now)
            every { repository.findByQuantumIdAndSnoozeGreaterThanEqual(any(), eq(now)) } returns snoozePref
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getSnoozePreference()

            verify { repository.findByQuantumIdAndSnoozeGreaterThanEqual(quantumId, now) }
            confirmVerified(repository)

            assertThat(returnValue).isNotNull
            assertThat(returnValue.snooze).isEqualTo(snoozePref.snooze)
        }

        @Test
        fun `Preference with past date exists in the database`() {
            val quantumId = "XYZ"
            every { repository.findByQuantumIdAndSnoozeGreaterThanEqual(any(), eq(now)) } returns null
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getSnoozePreference()

            verify { repository.findByQuantumIdAndSnoozeGreaterThanEqual(quantumId, now) }
            confirmVerified(repository)

            assertThat(returnValue).isNotNull
            assertThat(returnValue.snooze).isNull()
        }

        @Test
        fun `Preference doesn't exist in the database`() {
            val quantumId = "XYZ"
            every { repository.findByQuantumIdAndSnoozeGreaterThanEqual(any(), eq(now)) } returns null
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getSnoozePreference()

            verify { repository.findByQuantumIdAndSnoozeGreaterThanEqual(quantumId, now) }
            confirmVerified(repository)

            assertThat(returnValue).isNotNull
            assertThat(returnValue.snooze).isNull()
        }
    }

    @Nested
    @DisplayName("Update Snooze Preference tests")
    inner class UpdatePreferenceTests {

        @Test
        fun `Updates an existing preference with a future date`() {
            val quantumId = "XYZ"
            val snoozePref = SnoozePreference(quantumId, now.plusDays(1))
            every { repository.findByQuantumIdAndSnoozeGreaterThanEqual(any(), eq(now)) } returns snoozePref
            every { authenticationFacade.currentUsername } returns quantumId
            every { repository.save(snoozePref) } returns snoozePref

            val newPrefDate = now.plusDays(3)
            // This should equals() with the one created in the service code
            val compareSnoozePref = SnoozePreference(quantumId, newPrefDate)
            every { repository.save(compareSnoozePref) } returns compareSnoozePref
            service.createOrUpdateSnoozePreference(newPrefDate)

            verify { repository.findByQuantumIdAndSnoozeGreaterThanEqual(quantumId, now) }
            verify { repository.save(compareSnoozePref) }
            confirmVerified(repository)
        }

        @Test
        fun `Updates an existing preference with an older date`() {
            val quantumId = "XYZ"
            val snoozePref = SnoozePreference(quantumId, now.plusDays(1))
            every { repository.findByQuantumIdAndSnoozeGreaterThanEqual(any(), eq(now)) } returns snoozePref
            every { authenticationFacade.currentUsername } returns quantumId

            val newPrefDate = now.minusDays(3)
            val compareSnoozePref = SnoozePreference(quantumId, newPrefDate)
            every { repository.save(compareSnoozePref) } returns compareSnoozePref
            service.createOrUpdateSnoozePreference(newPrefDate)

            verify { repository.findByQuantumIdAndSnoozeGreaterThanEqual(quantumId, now) }
            verify { repository.save(compareSnoozePref) }
            confirmVerified(repository)
        }

        @Test
        fun `Creates a new preference with a future date`() {
            val quantumId = "XYZ"
            every { repository.findByQuantumIdAndSnoozeGreaterThanEqual(any(), eq(now)) } returns null
            every { authenticationFacade.currentUsername } returns quantumId

            val newPrefDate = now.plusDays(3)
            val compareSnoozePref = SnoozePreference(quantumId, newPrefDate)
            every { repository.save(compareSnoozePref) } returns compareSnoozePref
            service.createOrUpdateSnoozePreference(newPrefDate)

            verify { repository.findByQuantumIdAndSnoozeGreaterThanEqual(quantumId, now) }
            verify { repository.save(compareSnoozePref) }
            confirmVerified(repository)
        }

        @Test
        fun `Creates a new preference with an older date`() {
            val quantumId = "XYZ"
            every { repository.findByQuantumIdAndSnoozeGreaterThanEqual(any(), eq(now)) } returns null
            every { authenticationFacade.currentUsername } returns quantumId

            val newPrefDate = now.minusDays(3)
            val compareSnoozePref = SnoozePreference(quantumId, newPrefDate)
            every { repository.save(compareSnoozePref) } returns compareSnoozePref
            service.createOrUpdateSnoozePreference(newPrefDate)

            verify { repository.findByQuantumIdAndSnoozeGreaterThanEqual(quantumId, now) }
            verify { repository.save(compareSnoozePref) }
            confirmVerified(repository)
        }

    }


}