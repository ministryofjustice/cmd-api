package uk.gov.justice.digital.hmpps.cmd.api.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.cmd.api.model.UserPreference
import java.time.LocalDate
import java.util.*

@Repository
interface UserPreferenceRepository : CrudRepository<UserPreference, UUID> {

   fun findByQuantumIdAndSnoozeUntilGreaterThanEqual(quantumId: String, date: LocalDate): UserPreference?

   fun findByQuantumId(quantumId: String): UserPreference?
}
