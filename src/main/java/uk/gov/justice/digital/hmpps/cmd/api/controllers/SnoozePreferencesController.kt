package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.controllers

import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto.PreferenceDto
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto.UpdateSnoozeRequest
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.service.SnoozeNotificationPreferenceService


@RestController
@RequestMapping(produces = [APPLICATION_JSON_VALUE])
class SnoozePreferencesController(val snoozeNotificationPreferenceService: SnoozeNotificationPreferenceService) {

    @GetMapping("/preferences/notifications")
    fun getNotificationPreferences(): ResponseEntity<PreferenceDto> {
        return ResponseEntity.ok(snoozeNotificationPreferenceService.getSnoozePreference())
    }

    @PutMapping("/preferences/notifications/snooze")
    fun updateSnoozeNotificationPreferences(@RequestBody request: UpdateSnoozeRequest): ResponseEntity<Void> {
        snoozeNotificationPreferenceService.createOrUpdateSnoozePreference(request.snooze)
        return ResponseEntity.ok().build()
    }
}

