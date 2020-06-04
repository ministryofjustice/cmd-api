package uk.gov.justice.digital.hmpps.cmd.api.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Api(tags = ["cmd-notify-api"])
@RestController
@RequestMapping(
        value = ["cmd-notify-api"],
        produces = [MediaType.APPLICATION_JSON_VALUE])
class ProbationAreaCodesController() {

    @GetMapping(path = ["/test"])
    @ApiOperation(value = "Retrieve all Probation Area codes", nickname = "Retrieve all Probation Area codes")
    @ApiResponses(value = [
        ApiResponse(code = 200, message = "OK", response = String::class)
    ])
    fun getNothing(): String = "Stub endpoint";
}