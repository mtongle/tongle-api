package com.tongle.tongleapi.exception

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<Map<String, Any>> {
        logger.error("Error occurred: ${ex.message}", ex)

        val errorResponse = mapOf<String, Any>(
            "error" to "Internal Server Error",
            "message" to (ex.message ?: "Unknown error"),
            "timestamp" to Instant.now().toString()
        )

        return ResponseEntity.status(500)
            .body(errorResponse)
    }
}