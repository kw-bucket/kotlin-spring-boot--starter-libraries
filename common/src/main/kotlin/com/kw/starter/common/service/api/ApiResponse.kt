package com.kw.starter.common.service.api

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus

sealed class ApiResponse<out T> {
    abstract val httpStatus: HttpStatus
    abstract val httpHeaders: HttpHeaders?
    abstract val body: T?

    data class Success<out T>(
        override val httpStatus: HttpStatus,
        override val httpHeaders: HttpHeaders? = null,
        override val body: T? = null,
    ) : ApiResponse<T>()

    data class Failure<out T>(
        override val httpStatus: HttpStatus,
        override val httpHeaders: HttpHeaders? = null,
        override val body: T? = null,
    ) : ApiResponse<T>()

    data class Error(
        override val httpStatus: HttpStatus,
        override val httpHeaders: HttpHeaders? = null,
        override val body: Nothing? = null,
        val bodyAsString: String? = null,
        val cause: Throwable? = null,
    ) : ApiResponse<Nothing>()
}
