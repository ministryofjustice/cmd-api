package uk.gov.justice.digital.hmpps.cmd.api.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.cmd.api.dto.UpdateNotificationDetailsRequest
import uk.gov.justice.digital.hmpps.cmd.api.dto.UpdateSnoozeUntilRequest
import uk.gov.justice.digital.hmpps.cmd.api.dto.UserPreferenceDto
import uk.gov.justice.digital.hmpps.cmd.api.service.UserPreferenceService
import javax.validation.Valid

@RestController
@RequestMapping(produces = [APPLICATION_JSON_VALUE])
class UserPreferenceController(private val userPreferenceService: UserPreferenceService) {

  @Operation(
    summary = "Retrieve all preferences for a user",
    responses = [
      ApiResponse(
        responseCode = "200",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = UserPreferenceDto::class))]
      )
    ]
  )
  @GetMapping("/preferences/notifications")
  fun getNotificationPreferences(): ResponseEntity<UserPreferenceDto> {
    return ResponseEntity.ok(userPreferenceService.getUserPreference())
  }

  @Operation(
    summary = "Update the notification snooze until preference for a user",
    responses = [
      ApiResponse(
        responseCode = "200"
      )
    ]
  )
  @PutMapping("/preferences/notifications/snooze")
  fun updateSnoozeNotification(@RequestBody untilRequest: UpdateSnoozeUntilRequest): ResponseEntity<Void> {
    userPreferenceService.updateSnoozePreference(untilRequest.snoozeUntil)
    return ResponseEntity.ok().build()
  }

  @Operation(
    summary = "Update the notification details for a user",
    responses = [
      ApiResponse(
        responseCode = "200"
      )
    ]
  )
  @PutMapping("/preferences/notifications/details")
  fun updateNotificationDetails(@Valid @RequestBody detailsRequest: UpdateNotificationDetailsRequest): ResponseEntity<Void> {
    userPreferenceService.updateNotificationDetails(detailsRequest.email, detailsRequest.sms, detailsRequest.commPref)
    return ResponseEntity.ok().build()
  }
}
