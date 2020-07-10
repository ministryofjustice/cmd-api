package uk.gov.justice.digital.hmpps.cmd.api.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.cmd.api.model.UserPreference
import java.util.*

@Repository
interface UserPreferenceRepository : CrudRepository<UserPreference, UUID> {

   fun findByQuantumId(quantumId: String): UserPreference?
}
