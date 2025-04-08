package com.kw.starter.common.service.api

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.hc.client5.http.ConnectTimeoutException
import org.apache.hc.core5.http.ConnectionRequestTimeoutException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponents
import java.lang.reflect.Type
import java.net.SocketTimeoutException

open class ApiService(
    private val restTemplate: RestTemplate,
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun <T> execute(
        httpMethod: HttpMethod,
        uriComponents: UriComponents,
        httpEntity: HttpEntity<Any>?,
        responseType: ParameterizedTypeReference<T>,
    ): ApiResponse<T> =
        try {
            val response = call(httpMethod, uriComponents, httpEntity, responseType)
            val httpStatus = HttpStatus.valueOf(response.statusCode.value())

            if (response.statusCode.is2xxSuccessful) {
                ApiResponse.Success(
                    httpStatus = httpStatus,
                    httpHeaders = response.headers,
                    body = response.body,
                )
            } else {
                ApiResponse.Failure(
                    httpStatus = httpStatus,
                    httpHeaders = response.headers,
                    body = response.body,
                )
            }
        } catch (ex: HttpStatusCodeException) {
            logger.error(ex.message)

            ApiResponse.Error(
                httpStatus = HttpStatus.valueOf(ex.statusCode.value()),
                httpHeaders = ex.responseHeaders,
                bodyAsString = ex.responseBodyAsString,
                cause = ex,
            )
        } catch (ex: Exception) {
            logger.error(ex.message)

            val (httpStatus, cause) =
                when (ex) {
                    is ResourceAccessException ->
                        when (val cause: Throwable? = ex.cause) {
                            is ConnectionRequestTimeoutException,
                            is ConnectTimeoutException,
                            -> Pair(HttpStatus.INTERNAL_SERVER_ERROR, cause)
                            is SocketTimeoutException -> Pair(HttpStatus.GATEWAY_TIMEOUT, cause)
                            else -> Pair(HttpStatus.INTERNAL_SERVER_ERROR, ex)
                        }

                    else -> Pair(HttpStatus.INTERNAL_SERVER_ERROR, ex)
                }

            ApiResponse.Error(
                httpStatus = httpStatus,
                cause = cause,
            )
        }

    @Throws(
        HttpStatusCodeException::class,
        RestClientException::class,
    )
    fun <T> call(
        httpMethod: HttpMethod,
        uriComponents: UriComponents,
        httpEntity: HttpEntity<Any>?,
        responseType: ParameterizedTypeReference<T>,
    ): ResponseEntity<T> =
        try {
            restTemplate.exchange(
                uriComponents.toUri(),
                httpMethod,
                httpEntity,
                responseType,
            )
        } catch (ex: HttpStatusCodeException) {
            handleHttpException(ex, responseType.type)
        } catch (ex: RestClientException) {
            throw ex
        }

    @Throws(HttpStatusCodeException::class)
    private fun <T> handleHttpException(
        httpException: HttpStatusCodeException,
        responseType: Type,
    ): ResponseEntity<T> =
        try {
            val mapper =
                jacksonObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .registerModule(JavaTimeModule())

            val appResponse: T =
                mapper.readValue(
                    httpException.responseBodyAsString,
                    object : TypeReference<T>() {
                        override fun getType(): Type = responseType
                    },
                )

            ResponseEntity
                .status(httpException.statusCode)
                .headers(httpException.responseHeaders)
                .body(appResponse)
        } catch (ex: Exception) {
            throw httpException
        }
}
