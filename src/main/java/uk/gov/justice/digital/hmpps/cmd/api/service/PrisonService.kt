package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.Prison
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.repository.PrisonRepository

@Service
class PrisonService(val repository: PrisonRepository) {

    fun getAllPrisons(): Collection<Prison> {
        log.debug("Finding all prisons")
        val prisons = repository.findAll()
        log.info("Found ${prisons.size} prisons")
        return prisons
    }

    companion object {
        private val log = LoggerFactory.getLogger(PrisonService::class.java)
    }
}