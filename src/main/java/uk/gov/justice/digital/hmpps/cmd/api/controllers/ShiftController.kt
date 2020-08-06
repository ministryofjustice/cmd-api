package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto.DayModelDto
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto.DayEventDto
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.service.ShiftService
import java.time.LocalDate
import java.util.*

@Api(tags = ["shifts"])
@RestController
@RequestMapping(produces = [APPLICATION_JSON_VALUE])
class ShiftController(val shiftService: ShiftService) {

    @ApiOperation(value = "Retrieve all shifts for a user between two dates")
    @ApiResponses(value = [
        ApiResponse(code = 200, message = "OK")
    ])
    @GetMapping("/shifts")
    fun getShifts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: Optional<LocalDate>,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: Optional<LocalDate>): ResponseEntity<Collection<DayModelDto>> {
        val result = shiftService.getShiftsBetween(from, to)
        return ResponseEntity.ok(result)
    }

    @ApiOperation(value = "Retrieve a shift for a user for a specific")
    @ApiResponses(value = [
        ApiResponse(code = 200, message = "OK")
    ])
    @GetMapping("/shifts/tasks")
    fun getShift(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: Optional<LocalDate>): ResponseEntity<Collection<DayEventDto>> {
        val result = shiftService.getShiftFor(date)
        return ResponseEntity.ok(result)
    }

    @ApiOperation(value = "Retrieve all overtime for a user between two dates")
    @ApiResponses(value = [
        ApiResponse(code = 200, message = "OK")
    ])
    @GetMapping("/shifts/overtime")
    fun getOvertimeShifts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: Optional<LocalDate>,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: Optional<LocalDate>): ResponseEntity<Collection<DayModelDto>> {
        return ResponseEntity.ok(listOf())//shiftService.getOvertimeShiftsBetween(from, to))
    }

    @ApiOperation(value = "Retrieve a shift for a user for a specific")
    @ApiResponses(value = [
        ApiResponse(code = 200, message = "OK")
    ])
    @GetMapping("/shifts/overtime/tasks")
    fun getOvertimeShift(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: Optional<LocalDate>): ResponseEntity<Collection<DayEventDto>> {
        return ResponseEntity.ok(listOf())//shiftService.getOvertimeShiftFor(date))
    }

}

