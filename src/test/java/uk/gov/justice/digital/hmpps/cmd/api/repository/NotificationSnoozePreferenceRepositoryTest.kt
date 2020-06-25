package uk.gov.justice.digital.hmpps.cmd.api.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.SnoozePreference
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.repository.NotificationSnoozePreferenceRepository
import java.time.LocalDate

val now: LocalDate = LocalDate.now();

@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@WithAnonymousUser

class NotificationSnoozePreferenceRepositoryTest(
        @Autowired val repository: NotificationSnoozePreferenceRepository
) {

    @BeforeEach
    fun setUp() {
        repository.deleteAll();
        TestTransaction.flagForCommit()
        TestTransaction.end()
        TestTransaction.start()
    }

    @Test
    fun shouldFindValidPreference() {
        val quantumId = "XYZ"
        repository.save(SnoozePreference(quantumId,now.plusDays(20)))

        TestTransaction.flagForCommit()
        TestTransaction.end()
        TestTransaction.start()

        val preference = repository.findByQuantumIdAndSnoozeGreaterThanEqual(quantumId, now);

        assertThat(preference.quantumId).isEqualTo(quantumId)
        assertThat(preference.snooze).isEqualTo(now.plusDays(20))
    }

    @Test
    fun shouldFindPreferenceForToday() {
        val quantumId = "XYZ"
        repository.save(SnoozePreference(quantumId,now))

        TestTransaction.flagForCommit()
        TestTransaction.end()
        TestTransaction.start()

        val preference = repository.findByQuantumIdAndSnoozeGreaterThanEqual(quantumId, now);

        assertThat(preference.quantumId).isEqualTo(quantumId)
        assertThat(preference.snooze).isEqualTo(now)
    }

    @Test()
    fun shouldNotFindOutOfDatePreference() {
        val quantumId = "XYZ"
        repository.save(SnoozePreference(quantumId,now.minusDays(10)))

        TestTransaction.flagForCommit()
        TestTransaction.end()
        TestTransaction.start()

        val exception = Assertions.assertThrows(EmptyResultDataAccessException::class.java) {
            repository.findByQuantumIdAndSnoozeGreaterThanEqual(quantumId, now);
        }

    }


}