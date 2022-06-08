package uk.gov.justice.digital.hmpps.cmd.api.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.cmd.api.dto.NotificationDto
import uk.gov.justice.digital.hmpps.cmd.api.service.DryRunNotificationService
import java.time.LocalDate
import java.util.Optional

@RestController
@RequestMapping(produces = [APPLICATION_JSON_VALUE])
@Tag(name = "notifications")
class NotificationController(private val notificationService: DryRunNotificationService) {

  @Operation(summary = "Retrieve all notifications for a user between two dates")
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
