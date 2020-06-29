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
        fun `Should get preference with future date`() {
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
        fun `Should get preference with today's date`() {
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
        fun `Should get preference with past date`() {
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
        fun `Should handle preference not found`() {
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
        fun `Should updates an existing preference`() {
            val quantumId = "XYZ"
            val snoozePref = SnoozePreference(quantumId, now.plusDays(1))
            every { repository.findByQuantumId(any()) } returns snoozePref
            every { authenticationFacade.currentUsername } returns quantumId
            every { repository.save(snoozePref) } returns snoozePref

            val newPrefDate = now.plusDays(3)
            // This should equals() with the one created in the service code
            val compareSnoozePref = SnoozePreference(quantumId, newPrefDate)
            every { repository.save(compareSnoozePref) } returns compareSnoozePref
            service.createOrUpdateSnoozePreference(newPrefDate)

            verify { repository.findByQuantumId(quantumId) }
            verify { repository.save(compareSnoozePref) }
            confirmVerified(repository)
        }

        @Test
        fun `Should update an existing preference that has an older date`() {
            val quantumId = "XYZ"
            val snoozePref = SnoozePreference(quantumId, now.plusDays(1))
            every { repository.findByQuantumId(any()) } returns snoozePref
            every { authenticationFacade.currentUsername } returns quantumId

            val newPrefDate = now.minusDays(3)
            val compareSnoozePref = SnoozePreference(quantumId, newPrefDate)
            every { repository.save(compareSnoozePref) } returns compareSnoozePref
            service.createOrUpdateSnoozePreference(newPrefDate)

            verify { repository.findByQuantumId(quantumId) }
            verify { repository.save(compareSnoozePref) }
            confirmVerified(repository)
        }

        @Test
        fun `Should create a new preference with a future date`() {
            val quantumId = "XYZ"
            every { repository.findByQuantumId(any()) } returns null
            every { authenticationFacade.currentUsername } returns quantumId

            val newPrefDate = now.plusDays(3)
            val compareSnoozePref = SnoozePreference(quantumId, newPrefDate)
            every { repository.save(compareSnoozePref) } returns compareSnoozePref
            service.createOrUpdateSnoozePreference(newPrefDate)

            verify { repository.findByQuantumId(quantumId) }
            verify { repository.save(compareSnoozePref) }
            confirmVerified(repository)
        }

        @Test
        fun `Should create a new preference with an older date`() {
            val quantumId = "XYZ"
            every { repository.findByQuantumId(any()) } returns null
            every { authenticationFacade.currentUsername } returns quantumId

            val newPrefDate = now.minusDays(3)
            val compareSnoozePref = SnoozePreference(quantumId, newPrefDate)
            every { repository.save(compareSnoozePref) } returns compareSnoozePref
            service.createOrUpdateSnoozePreference(newPrefDate)

            verify { repository.findByQuantumId(quantumId) }
            verify { repository.save(compareSnoozePref) }
            confirmVerified(repository)
        }

    }


}