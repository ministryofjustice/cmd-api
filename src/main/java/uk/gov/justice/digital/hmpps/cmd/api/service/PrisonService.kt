package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.cmd.api.service.UserPreferenceService
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.Prison
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.repository.PrisonRepository


@Service
@Transactional
class PrisonService(val repository: PrisonRepository) {

    fun getAllPrisons(): Iterable<Prison> {
        log.debug("Fetching prisons")
        val prisons = repository.findAll()
        log.info("Found prisons")
        return prisons
    }

    companion object {
        private val log = LoggerFactory.getLogger(PrisonService::class.java)
    }
}