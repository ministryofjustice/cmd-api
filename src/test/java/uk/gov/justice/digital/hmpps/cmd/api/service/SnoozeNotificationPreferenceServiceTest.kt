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
import org.springframework.security.oauth2.jwt.Jwt
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.SnoozePreference
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.repository.SnoozePreferenceRepository
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.service.SnoozeNotificationPreferenceService
import java.time.LocalDate
import java.util.*

@ExtendWith(MockKExtension::class)
@DisplayName("Snooze Notification Preference Service tests")
internal class SnoozeNotificationPreferenceServiceTest {
    private val repository: SnoozePreferenceRepository = mockk(relaxUnitFun = true)
    private val jwt: Jwt = mockk(relaxUnitFun = true)
    private val now = LocalDate.of(2020,6,25)

    private val service = SnoozeNotificationPreferenceService(repository, now)

    @BeforeEach
    fun resetAllMocks() {
        clearMocks(repository)
    }

    @Test
    fun `Preference with future date exists in the database`() {
        val quantumId = "XYZ"
        val prefDate = now.plusDays(1)
        val snoozePref = SnoozePreference(quantumId, prefDate)
        every { repository.findByQuantumIdAndSnoozeGreaterThanEqual(any(), eq(now)) } returns Optional.ofNullable(snoozePref)
        every { jwt.subject } returns quantumId

        val returnValue = service.getSnoozePreference(jwt)

        verify {repository.findByQuantumIdAndSnoozeGreaterThanEqual(quantumId, now)}

        assertThat(returnValue).isNotNull
        assertThat(returnValue.snoozeDate).isEqualTo(prefDate)
    }

    @Test
    fun `Preference with today's date exists in the database`() {
        val quantumId = "XYZ"
        val prefDate = now
        val snoozePref = SnoozePreference(quantumId, prefDate)
        every { repository.findByQuantumIdAndSnoozeGreaterThanEqual(any(), eq(now)) } returns Optional.ofNullable(snoozePref)
        every { jwt.subject } returns quantumId

        val returnValue = service.getSnoozePreference(jwt)

        verify {repository.findByQuantumIdAndSnoozeGreaterThanEqual(quantumId, now)}

        assertThat(returnValue).isNotNull
        assertThat(returnValue.snoozeDate).isEqualTo(prefDate)
    }

    @Test
    fun `Preference with past date exists in the database`() {
        val quantumId = "XYZ"
        every { repository.findByQuantumIdAndSnoozeGreaterThanEqual(any(), eq(now)) } returns Optional.empty()
        every { jwt.subject } returns quantumId

        val returnValue = service.getSnoozePreference(jwt)

        verify {repository.findByQuantumIdAndSnoozeGreaterThanEqual(quantumId, now)}

        assertThat(returnValue).isNotNull
        assertThat(returnValue.snoozeDate).isEqualTo(LocalDate.MIN)
    }

    @Test
    fun `Preference doesn't exist in the database`() {
        val quantumId = "XYZ"
        every { repository.findByQuantumIdAndSnoozeGreaterThanEqual(any(), eq(now)) } returns Optional.empty()
        every { jwt.subject } returns quantumId

        val returnValue = service.getSnoozePreference(jwt)

        verify {repository.findByQuantumIdAndSnoozeGreaterThanEqual(quantumId, now)}

        assertThat(returnValue).isNotNull
        assertThat(returnValue.snoozeDate).isEqualTo(LocalDate.MIN)
    }
}