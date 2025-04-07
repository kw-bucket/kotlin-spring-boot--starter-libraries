package com.kw.starter.common.controller.advice

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.kw.starter.common.constant.ApiOutputStatus
import com.kw.starter.common.dto.ApiOutput
import com.kw.starter.common.exception.ApplicationException
import com.kw.starter.common.extension.string.toSnakeCase
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@ControllerAdvice
class ApplicationControllerAdvice {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Value("\${starter.application-code:APP}")
    private val applicationCode: String = "APP"

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ApiOutput<Nothing>> =
        buildResponse(
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
            status = ExceptionStatus.E5000,
            description = ex.message,
        ).also {
            logger.error("Exception: {}", ex.stackTraceToString())
        }

    @ExceptionHandler(
        value = [
            HttpMessageNotReadableException::class,
            MethodArgumentNotValidException::class,
            MissingServletRequestParameterException::class,
            MethodArgumentTypeMismatchException::class,
            InvalidFormatException::class,
            IllegalArgumentException::class,
            IllegalStateException::class,
        ],
    )
    fun handleInvalidRequest(ex: Exception): ResponseEntity<ApiOutput<Nothing>> {
        logger.error("Invalid Request: {}", ex.localizedMessage)

        val description: String? =
            when (ex) {
                is MethodArgumentNotValidException -> {
                    ex.fieldError.let { "${it?.field?.toSnakeCase()} - ${it?.defaultMessage}" }
                }
                is HttpMessageNotReadableException -> "Invalid Request Format"
                is IllegalArgumentException,
                is IllegalStateException,
                -> ex.message

                else -> null
            }

        return buildResponse(HttpStatus.BAD_REQUEST, ExceptionStatus.E4000, description)
    }

    @ExceptionHandler(ApplicationException::class)
    fun handleAppException(ex: ApplicationException): ResponseEntity<ApiOutput<Nothing>> =
        buildResponse(ex.httpStatus, ex.apiOutputStatus, ex.description)

    private fun buildResponse(
        httpStatus: HttpStatus,
        status: ApiOutputStatus,
        description: String? = null,
    ): ResponseEntity<ApiOutput<Nothing>> =
        ApiOutput.fromStatus<Nothing>(status, description).let { ResponseEntity.status(httpStatus).body(it) }

    private fun buildResponse(
        httpStatus: HttpStatus,
        status: ExceptionStatus,
        description: String? = null,
    ): ResponseEntity<ApiOutput<Nothing>> {
        val code = "$applicationCode${status.code}"
        val apiOutput: ApiOutput<Nothing> =
            ApiOutput.fromCustomStatus(
                code = code,
                message = status.message,
                description = description ?: status.description,
            )

        return ResponseEntity.status(httpStatus).body(apiOutput)
    }
}

private enum class ExceptionStatus(
    override val code: String,
    override val message: String,
    override val description: String,
) : ApiOutputStatus {
    E4000("4000", "Bad Request", "Bad Request"),
    E5000("5000", "Internal Server Error", "Internal Server Error"),
}
