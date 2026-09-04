package com.syfe.personalfinance.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.core.AuthenticationException
import org.springframework.validation.FieldError
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(ex: CustomException, request: WebRequest): ResponseEntity<Map<String, Any>> {
        val body = mutableMapOf<String, Any>(
            "timestamp" to LocalDateTime.now().toString(),
            "status" to ex.status.value(),
            "error" to ex.status.reasonPhrase,
            "message" to (ex.message ?: "An error occurred"),
            "path" to request.getDescription(false).replace("uri=", "")
        )
        return ResponseEntity(body, ex.status)
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialsException(ex: BadCredentialsException, request: WebRequest): ResponseEntity<Map<String, Any>> {
        val body = mutableMapOf<String, Any>(
            "timestamp" to LocalDateTime.now().toString(),
            "status" to HttpStatus.UNAUTHORIZED.value(),
            "error" to "Unauthorized",
            "message" to "Invalid username or password",
            "path" to request.getDescription(false).replace("uri=", "")
        )
        return ResponseEntity(body, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(InternalAuthenticationServiceException::class)
    fun handleInternalAuthServiceException(ex: InternalAuthenticationServiceException, request: WebRequest): ResponseEntity<Map<String, Any>> {
        val body = mutableMapOf<String, Any>(
            "timestamp" to LocalDateTime.now().toString(),
            "status" to HttpStatus.UNAUTHORIZED.value(),
            "error" to "Unauthorized",
            "message" to "Invalid username or password",
            "path" to request.getDescription(false).replace("uri=", "")
        )
        return ResponseEntity(body, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(ex: AuthenticationException, request: WebRequest): ResponseEntity<Map<String, Any>> {
        val body = mutableMapOf<String, Any>(
            "timestamp" to LocalDateTime.now().toString(),
            "status" to HttpStatus.UNAUTHORIZED.value(),
            "error" to "Unauthorized",
            "message" to (ex.message ?: "Authentication failed"),
            "path" to request.getDescription(false).replace("uri=", "")
        )
        return ResponseEntity(body, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException, request: WebRequest): ResponseEntity<Map<String, Any>> {
        val errors = mutableMapOf<String, String>()
        ex.bindingResult.allErrors.forEach { error ->
            val fieldName = (error as? FieldError)?.field ?: error.objectName
            val errorMessage = error.defaultMessage ?: "Invalid value"
            errors[fieldName] = errorMessage
        }

        val body = mutableMapOf<String, Any>(
            "timestamp" to LocalDateTime.now().toString(),
            "status" to HttpStatus.BAD_REQUEST.value(),
            "error" to "Bad Request",
            "message" to "Validation failed",
            "data" to errors,
            "path" to request.getDescription(false).replace("uri=", "")
        )
        return ResponseEntity(body, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleMediaTypeNotSupportedException(ex: HttpMediaTypeNotSupportedException, request: WebRequest): ResponseEntity<Map<String, Any>> {
        val body = mutableMapOf<String, Any>(
            "timestamp" to LocalDateTime.now().toString(),
            "status" to HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
            "error" to "Unsupported Media Type",
            "message" to "Unsupported media type: Please send request as 'application/json'",
            "path" to request.getDescription(false).replace("uri=", "")
        )
        return ResponseEntity(body, HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleMessageNotReadableException(ex: HttpMessageNotReadableException, request: WebRequest): ResponseEntity<Map<String, Any>> {
        val detail = ex.mostSpecificCause?.message ?: ex.message ?: ""
        val body = mutableMapOf<String, Any>(
            "timestamp" to LocalDateTime.now().toString(),
            "status" to HttpStatus.BAD_REQUEST.value(),
            "error" to "Bad Request",
            "message" to "Malformed JSON request body: $detail",
            "path" to request.getDescription(false).replace("uri=", "")
        )
        return ResponseEntity(body, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception, request: WebRequest): ResponseEntity<Map<String, Any>> {
        val body = mutableMapOf<String, Any>(
            "timestamp" to LocalDateTime.now().toString(),
            "status" to HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "error" to "Internal Server Error",
            "message" to "An unexpected server error occurred: ${ex.message}",
            "path" to request.getDescription(false).replace("uri=", "")
        )
        return ResponseEntity(body, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
