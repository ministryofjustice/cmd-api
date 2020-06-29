package uk.gov.justice.digital.hmpps.cmd.api.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.SnoozePreference
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.repository.SnoozePreferenceRepository
import java.time.LocalDate

@ActiveProfiles("test")
@DataJpaTest
class SnoozePreferenceRepositoryTest(
        @Autowired val repository: SnoozePreferenceRepository
) {

    private val now: LocalDate = LocalDate.now()

    @BeforeEach
    fun resetAllMocks() {
        repository.deleteAll()
    }

    @Test
    fun `Should return a preference with a future date`() {
        val quantumId = "XYZ"
        repository.save(SnoozePreference(quantumId, now.plusDays(20)))

        val pref = repository.findByQuantumIdAndSnoozeGreaterThanEqual(quantumId, now)
        assertThat(pref).isNotNull

        assertThat(pref?.quantumId).isEqualTo(quantumId)
        assertThat(pref?.snooze).isEqualTo(now.plusDays(20))
    }

    @Test
    fun `Should return a preference with today's date`() {
        val quantumId = "XYZ"
        repository.save(SnoozePreference(quantumId, now))

        val pref = repository.findByQuantumIdAndSnoozeGreaterThanEqual(quantumId, now)
        assertThat(pref).isNotNull

        assertThat(pref?.quantumId).isEqualTo(quantumId)
        assertThat(pref?.snooze).isEqualTo(now)
    }

    @Test
    fun `Should not return a preference with a past date`() {
        val quantumId = "XYZ"
        repository.save(SnoozePreference(quantumId,now.minusDays(10)))

        val pref = repository.findByQuantumIdAndSnoozeGreaterThanEqual(quantumId, now)
        assertThat(pref).isNull()
    }

}