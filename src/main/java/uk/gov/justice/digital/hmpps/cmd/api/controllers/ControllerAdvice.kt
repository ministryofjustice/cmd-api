package uk.gov.justice.digital.hmpps.cmd.api.controllers

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import uk.gov.justice.digital.hmpps.cmd.api.dto.ErrorResponse
import java.util.*
import java.util.function.Consumer


@RestControllerAdvice(basePackages = ["uk.gov.justice.digital.hmpps.cmd.api.controllers"])
class ControllerAdvice {
    @ExceptionHandler(RestClientResponseException::class)
    fun handleRestClientResponseException(e: RestClientResponseException): ResponseEntity<ByteArray> {
        log.error("Unexpected exception", e)
        return ResponseEntity
                .status(e.rawStatusCode)
                .body(e.responseBodyAsByteArray)
    }

    @ExceptionHandler(RestClientException::class)
    fun handleRestClientException(e: RestClientException): ResponseEntity<ErrorResponse> {
        log.error("Unexpected exception", e)
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse(status = (HttpStatus.INTERNAL_SERVER_ERROR.value()), developerMessage = (e.message)))
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(e: AccessDeniedException?): ResponseEntity<ErrorResponse> {
        log.debug("Forbidden (403) returned", e)
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse(status = (HttpStatus.FORBIDDEN.value())))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors: MutableMap<String, String> = HashMap()
        e.bindingResult.allErrors.forEach(Consumer { error: ObjectError ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.getDefaultMessage()
            if (errorMessage != null)
              errors[fieldName] = errorMessage
        })
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse(status = (HttpStatus.INTERNAL_SERVER_ERROR.value()), developerMessage = (errors.toString())))
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleValidationException(e: MissingServletRequestParameterException): ResponseEntity<ErrorResponse> {
        log.debug("Bad Request (400) returned", e)
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse(status = (HttpStatus.BAD_REQUEST.value()), developerMessage = (e.message)))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unexpected exception", e)
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse(status = (HttpStatus.INTERNAL_SERVER_ERROR.value()), developerMessage = (e.message)))
    }

    companion object {
        private val log = LoggerFactory.getLogger(ControllerAdvice::class.java)
    }
}
