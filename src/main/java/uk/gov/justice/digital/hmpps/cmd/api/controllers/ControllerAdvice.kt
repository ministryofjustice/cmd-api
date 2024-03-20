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
import org.springframework.web.servlet.resource.NoResourceFoundException
import uk.gov.justice.digital.hmpps.cmd.api.service.NotFoundException
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.util.function.Consumer

@RestControllerAdvice(basePackages = ["uk.gov.justice.digital.hmpps.cmd.api.controllers"])
class ControllerAdvice {
  @ExceptionHandler(RestClientResponseException::class)
  fun handleRestClientResponseException(e: RestClientResponseException): ResponseEntity<ByteArray> = ResponseEntity
    .status(e.statusCode)
    .body(e.responseBodyAsByteArray).also {
      log.error("Unexpected exception", e)
    }

  @ExceptionHandler(RestClientException::class)
  fun handleRestClientException(e: RestClientException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(HttpStatus.INTERNAL_SERVER_ERROR)
    .body(ErrorResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, developerMessage = e.message)).also {
      log.error("Unexpected exception", e)
    }

  @ExceptionHandler(AccessDeniedException::class)
  fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(HttpStatus.FORBIDDEN)
    .body(ErrorResponse(status = HttpStatus.FORBIDDEN)).also {
      log.debug("Forbidden (403) returned: {}", e.message)
    }

  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleValidationExceptions(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
    val errors: MutableMap<String, String> = HashMap()
    e.bindingResult.allErrors.forEach(
      Consumer { error: ObjectError ->
        val fieldName = (error as FieldError).field
        val errorMessage = error.getDefaultMessage()
        if (errorMessage != null) {
          errors[fieldName] = errorMessage
        }
      },
    )
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(ErrorResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, developerMessage = errors.toString()))
  }

  @ExceptionHandler(MissingServletRequestParameterException::class)
  fun handleValidationException(e: MissingServletRequestParameterException): ResponseEntity<ErrorResponse> =
    ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(ErrorResponse(status = HttpStatus.BAD_REQUEST, developerMessage = e.message)).also {
        log.debug("Bad Request (400) returned: {}", e.message)
      }

  @ExceptionHandler(NotFoundException::class)
  fun handleNotFoundException(e: NotFoundException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(HttpStatus.NOT_FOUND)
    .body(ErrorResponse(status = HttpStatus.NOT_FOUND, developerMessage = e.message))

  @ExceptionHandler(NoResourceFoundException::class)
  fun handleNoResourceFoundException(e: NoResourceFoundException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(HttpStatus.NOT_FOUND)
    .body(
      ErrorResponse(
        status = HttpStatus.NOT_FOUND,
        userMessage = "No resource found failure: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.info("No resource found exception: {}", e.message) }

  @ExceptionHandler(Exception::class)
  fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
    return ResponseEntity
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(ErrorResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, developerMessage = e.message)).also {
        log.error("Unexpected exception", e)
      }
  }

  companion object {
    private val log = LoggerFactory.getLogger(ControllerAdvice::class.java)
  }
}
