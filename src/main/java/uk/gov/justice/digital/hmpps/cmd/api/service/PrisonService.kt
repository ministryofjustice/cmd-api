package uk.gov.justice.digital.hmpps.cmd.api.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.cmd.api.client.Elite2ApiClient
import uk.gov.justice.digital.hmpps.cmd.api.model.Prison
import uk.gov.justice.digital.hmpps.cmd.api.repository.PrisonRepository

@Service
@Transactional
class PrisonService(private val repository: PrisonRepository,
                    private val elite2Client: Elite2ApiClient) {

    fun getAllPrisons(): Collection<Prison> {
        log.debug("Finding all prisons")
        val prisons = repository.findAll()
        log.info("Found ${prisons.size} prisons")
        return prisons
    }

    fun getPrisonForUser() : Prison? {
        val prisonId = elite2Client.getCurrentPrisonIdForUser()
        log.debug("Finding prison by id $prisonId")
        val prison = repository.findByPrisonId(prisonId)
        if(prison != null) {
            log.info("Found prison ${prison.prisonName} by id $prisonId")
        } else {
            log.warn("No prison found for id $prisonId")
        }
        return prison
    }

    companion object {
        private val log = LoggerFactory.getLogger(PrisonService::class.java)
    }
}