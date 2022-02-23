package uk.gov.justice.digital.hmpps.cmd.api.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.cmd.api.dto.NotificationDto
import uk.gov.justice.digital.hmpps.cmd.api.service.NotificationService
import java.time.LocalDate
import java.util.Optional

@RestController
@RequestMapping(produces = [APPLICATION_JSON_VALUE])
class NotificationController(private val notificationService: NotificationService) {

  @Operation(
    summary = "Retrieve all notifications for a user between two dates",
    responses = [
      ApiResponse(
        responseCode = "200",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = NotificationDto::class))]
      )
    ]
  )
  @GetMapping("/notifications")
  fun getNotifications(
    @RequestParam processOnRead: Optional<Boolean>,
    @RequestParam unprocessedOnly: Optional<Boolean>,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: Optional<LocalDate>,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: Optional<LocalDate>
  ): ResponseEntity<Collection<NotificationDto>> {
    return ResponseEntity.ok(notificationService.getNotifications(processOnRead, unprocessedOnly, from, to))
  }
}
