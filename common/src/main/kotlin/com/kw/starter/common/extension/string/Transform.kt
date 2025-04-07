package com.kw.starter.common.extension.string

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kw.starter.common.service.api.ApiResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus

val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()

/**
 * Transform string from camel case to snake case
 */
fun String.toSnakeCase(): String = camelRegex.replace(this) { "_${it.value}" }.lowercase()

/**
 * Parse json string into an object. otherwise, null
 */
inline fun <reified T : Any> String.parseOrNull(): T? {
    val logger: Logger = LoggerFactory.getLogger("StringExt")

    return try {
        jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(JavaTimeModule())
            .readValue(this, object : TypeReference<T>() {})
    } catch (e: Exception) {
        logger.error("{} Parse Error!!!", T::class.java.simpleName)
        e.printStackTrace()

        null
    }
}

/**
 * Find a resource with a string that provide the location of file
 */
fun String.asResource() = {}::class.java.getResource(this)?.readText() ?: "read resource fail: $this"

/**
 * Parse json string into a nullable object as body of ApiResponse.Success
 */
inline fun <reified T : Any> String.asApiResponseSuccess(): ApiResponse.Success<T> =
    this.parseOrNull<T>().let { ApiResponse.Success(httpStatus = HttpStatus.OK, httpHeaders = null, body = it) }

/**
 * Parse json string into a nullable object as body of ApiResponse.Failure
 */
inline fun <reified T : Any> String.asApiResponseFailure(
    httpStatus: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
): ApiResponse.Failure<T> =
    this.parseOrNull<T>().let {
        ApiResponse.Failure(httpStatus = httpStatus, httpHeaders = null, body = it)
    }

/**
 * Parse json string into a nullable object as bodyAsString of ApiResponse.Error
 */
fun String.asApiResponseError(httpStatus: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR): ApiResponse.Error =
    ApiResponse.Error(httpStatus = httpStatus, httpHeaders = null, bodyAsString = this)
