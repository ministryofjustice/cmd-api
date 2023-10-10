package uk.gov.justice.digital.hmpps.cmd.api.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.cmd.api.dto.ShiftDto
import uk.gov.justice.digital.hmpps.cmd.api.service.ShiftService
import java.time.LocalDate
import java.util.Optional

@RestController
@Tag(name = "shift controller")
@RequestMapping(produces = [APPLICATION_JSON_VALUE])
class ShiftController(private val shiftService: ShiftService) {

  @Operation(summary = "Retrieve all details for a user between two dates")
  @GetMapping("/user/details")
  @PreAuthorize("hasRole('ROLE_CMD')")
  fun getShifts(
    @RequestParam
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    from: Optional<LocalDate>,
    @RequestParam
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    to: Optional<LocalDate>,
  ): Collection<ShiftDto> = shiftService.getDetailsForUser(from, to)
}
