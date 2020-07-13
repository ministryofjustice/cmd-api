package uk.gov.justice.digital.hmpps.cmd.api.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.cmd.api.dto.UpdateSnoozeUntilRequest
import uk.gov.justice.digital.hmpps.cmd.api.dto.UserPreferenceDto
import uk.gov.justice.digital.hmpps.cmd.api.service.UserPreferenceService

@Api(tags = ["user-preferences"])
@RestController
@RequestMapping(produces = [APPLICATION_JSON_VALUE])
class UserPreferenceController(val userPreferenceService: UserPreferenceService) {

    @ApiOperation(value = "Retrieve all preferences for a user")
    @ApiResponses(value = [
        ApiResponse(code = 200, message = "OK", response = UserPreferenceDto::class)
    ])
    @GetMapping("/preferences/notifications")
    fun getNotificationPreferences(): ResponseEntity<UserPreferenceDto> {
        return ResponseEntity.ok(userPreferenceService.getUserPreference())
    }

    @ApiOperation(value = "Update the notification snooze until preference for a user")
    @ApiResponses(value = [
        ApiResponse(code = 200, message = "OK")
    ])
    @PutMapping("/preferences/notifications/snooze")
    fun updateSnoozeNotificationPreferences(@RequestBody untilRequest: UpdateSnoozeUntilRequest): ResponseEntity<Void> {
        userPreferenceService.updateSnoozePreference(untilRequest.snoozeUntil)
        return ResponseEntity.ok().build()
    }
}

