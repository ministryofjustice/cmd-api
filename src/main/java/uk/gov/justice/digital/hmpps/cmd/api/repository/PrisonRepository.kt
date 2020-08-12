package uk.gov.justice.digital.hmpps.cmd.api.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.cmd.api.model.Prison
import java.util.*

@Repository
interface PrisonRepository : CrudRepository<Prison, UUID> {
    override fun findAll(): Collection<Prison>
}
