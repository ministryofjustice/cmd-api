package uk.gov.justice.digital.hmpps.cmd.api.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto.PreferenceDto
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto.UpdateSnoozeRequest
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.service.SnoozeNotificationPreferenceService

@Api(tags = ["notification-preferences"])
@RestController
@RequestMapping(produces = [APPLICATION_JSON_VALUE])
class SnoozePreferencesController(val snoozeNotificationPreferenceService: SnoozeNotificationPreferenceService) {

    @ApiOperation(value = "Retrieve all notification preferences")
    @ApiResponses(value = [
        ApiResponse(code = 200, message = "OK", response = PreferenceDto::class)
    ])
    @GetMapping("/preferences/notifications")
    fun getNotificationPreferences(): ResponseEntity<PreferenceDto> {
        return ResponseEntity.ok(snoozeNotificationPreferenceService.getSnoozePreference())
    }

    @ApiOperation(value = "Update the snooze notification preference")
    @ApiResponses(value = [
        ApiResponse(code = 200, message = "OK")
    ])
    @PutMapping("/preferences/notifications/snooze")
    fun updateSnoozeNotificationPreferences(@RequestBody request: UpdateSnoozeRequest): ResponseEntity<Void> {
        snoozeNotificationPreferenceService.createOrUpdateSnoozePreference(request.snooze)
        return ResponseEntity.ok().build()
    }
}

