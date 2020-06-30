package uk.gov.justice.digital.hmpps.cmd.api.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.UserPreference
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.repository.UserPreferenceRepository
import java.time.LocalDate

@ActiveProfiles("test")
@DataJpaTest
class UserPreferenceRepositoryTest(
        @Autowired val repository: UserPreferenceRepository
) {

    private val now: LocalDate = LocalDate.now()

    @BeforeEach
    fun resetAllMocks() {
        repository.deleteAll()
    }

    @Nested
    @DisplayName("Get Snooze Preference tests")
    inner class GetPreferenceTests {

        @Test
        fun `Should return a preference with a future date`() {
            val quantumId = "XYZ"
            repository.save(UserPreference(quantumId, now.plusDays(20)))

            val pref = repository.findByQuantumIdAndSnoozeUntilGreaterThanEqual(quantumId, now)
            assertThat(pref).isNotNull

            assertThat(pref?.quantumId).isEqualTo(quantumId)
            assertThat(pref?.snoozeUntil).isEqualTo(now.plusDays(20))
        }

        @Test
        fun `Should return a preference with today's date`() {
            val quantumId = "XYZ"
            repository.save(UserPreference(quantumId, now))

            val pref = repository.findByQuantumIdAndSnoozeUntilGreaterThanEqual(quantumId, now)
            assertThat(pref).isNotNull

            assertThat(pref?.quantumId).isEqualTo(quantumId)
            assertThat(pref?.snoozeUntil).isEqualTo(now)
        }

        @Test
        fun `Should not return a preference with a past date`() {
            val quantumId = "XYZ"
            repository.save(UserPreference(quantumId, now.minusDays(10)))

            val pref = repository.findByQuantumIdAndSnoozeUntilGreaterThanEqual(quantumId, now)
            assertThat(pref).isNull()
        }

    }

    @Nested
    @DisplayName("Update Snooze Preference tests")
    inner class UpdatePreferenceTests {

        @Test
        fun `Should get a preference with a future date`() {
            val quantumId = "XYZ"
            repository.save(UserPreference(quantumId, now.plusDays(20)))

            val result = repository.findByQuantumId(quantumId)

            assertThat(result).isNotNull
            assertThat(result?.quantumId).isEqualTo(quantumId)
            assertThat(result?.snoozeUntil).isEqualTo(now.plusDays(20))
        }

        @Test
        fun `Should get preference with a past date`() {
            val quantumId = "XYZ"
            repository.save(UserPreference(quantumId, now.minusDays(10)))

            // If we use the other repository method `findByQuantumIdAndSnoozeGreaterThanEqual`
            // we might not return anything when there is actually an entry.
            val result = repository.findByQuantumId(quantumId)

            assertThat(result).isNotNull
            assertThat(result?.quantumId).isEqualTo(quantumId)
            assertThat(result?.snoozeUntil).isEqualTo(now.minusDays(10))
        }

    }

}