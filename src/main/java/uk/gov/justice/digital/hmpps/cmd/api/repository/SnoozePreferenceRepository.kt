package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.SnoozePreference
import java.time.LocalDate
import java.util.*

@Repository
interface SnoozePreferenceRepository : CrudRepository<SnoozePreference, UUID> {

   fun findByQuantumIdAndSnoozeGreaterThanEqual(quantumId : String, date : LocalDate) : Optional<SnoozePreference>
}