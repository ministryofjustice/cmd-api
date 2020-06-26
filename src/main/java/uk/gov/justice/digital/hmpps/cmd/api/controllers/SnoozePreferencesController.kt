package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.controllers

import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto.SnoozePreferenceDto
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.service.SnoozeNotificationPreferenceService


@RestController
@RequestMapping(produces = [APPLICATION_JSON_VALUE])
class SnoozePreferencesController(val snoozeNotificationPreferenceService: SnoozeNotificationPreferenceService, val authenticationFacade: AuthenticationFacade) {

    @GetMapping(path = ["/preferences/notifications"])
    fun getProbationArea(): ResponseEntity<SnoozePreferenceDto> {
        val userName: String = authenticationFacade.currentUsername
        return ResponseEntity.ok(snoozeNotificationPreferenceService.getSnoozePreference(userName))
    }

}
