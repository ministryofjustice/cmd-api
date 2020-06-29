package uk.gov.justice.digital.hmpps.cmd.api.service

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
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
    private val now = LocalDate.of(2020, 6, 25)
    private val clock = Clock.fixed(now.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
    private val service = SnoozeNotificationPreferenceService(repository, clock, authenticationFacade)

    @BeforeEach
    fun resetAllMocks() {
        clearMocks(repository)
    }

    @Test
    fun `Preference with future date exists in the database`() {
        val quantumId = "XYZ"
        val prefDate = now.plusDays(1)
        val snoozePref = SnoozePreference(quantumId, prefDate)
        every { repository.findByQuantumIdAndSnoozeGreaterThanEqual(any(), eq(now)) } returns snoozePref
        every { authenticationFacade.currentUsername } returns quantumId

        val returnValue = service.getSnoozePreference()

        verify { repository.findByQuantumIdAndSnoozeGreaterThanEqual(quantumId, now) }

        assertThat(returnValue).isNotNull
        assertThat(returnValue.snoozeDate).isEqualTo(prefDate)
    }

    @Test
    fun `Preference with today's date exists in the database`() {
        val quantumId = "XYZ"
        val prefDate = now
        val snoozePref = SnoozePreference(quantumId, prefDate)
        every { repository.findByQuantumIdAndSnoozeGreaterThanEqual(any(), eq(now)) } returns snoozePref
        every { authenticationFacade.currentUsername } returns quantumId

        val returnValue = service.getSnoozePreference()

        verify { repository.findByQuantumIdAndSnoozeGreaterThanEqual(quantumId, now) }

        assertThat(returnValue).isNotNull
        assertThat(returnValue.snoozeDate).isEqualTo(prefDate)
    }

    @Test
    fun `Preference with past date exists in the database`() {
        val quantumId = "XYZ"
        every { repository.findByQuantumIdAndSnoozeGreaterThanEqual(any(), eq(now)) } returns null
        every { authenticationFacade.currentUsername } returns quantumId

        val returnValue = service.getSnoozePreference()

        verify { repository.findByQuantumIdAndSnoozeGreaterThanEqual(quantumId, now) }

        assertThat(returnValue).isNotNull
        assertThat(returnValue.snoozeDate).isNull()
    }

    @Test
    fun `Preference doesn't exist in the database`() {
        val quantumId = "XYZ"
        every { repository.findByQuantumIdAndSnoozeGreaterThanEqual(any(), eq(now)) } returns null
        every { authenticationFacade.currentUsername } returns quantumId

        val returnValue = service.getSnoozePreference()

        verify { repository.findByQuantumIdAndSnoozeGreaterThanEqual(quantumId, now) }

        assertThat(returnValue).isNotNull
        assertThat(returnValue.snoozeDate).isNull()
    }
}