package uk.gov.justice.digital.hmpps.cmd.api.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
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
import java.util.*

@Api(tags = ["notifications"])
@RestController
@RequestMapping(produces = [APPLICATION_JSON_VALUE])
class NotificationController(val notificationService: NotificationService) {

    @ApiOperation(value = "Retrieve all notifications for a user between two dates")
    @ApiResponses(value = [
        ApiResponse(code = 200, message = "OK", response = NotificationDto::class)
    ])
    @GetMapping("/notifications")
    fun getNotifications(
            @RequestParam processOnRead: Optional<Boolean>,
            @RequestParam unprocessedOnly: Optional<Boolean>,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: Optional<LocalDate>,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: Optional<LocalDate>): ResponseEntity<Collection<NotificationDto>> {
        return ResponseEntity.ok(notificationService.getNotifications(processOnRead, unprocessedOnly, from, to))
    }
}

