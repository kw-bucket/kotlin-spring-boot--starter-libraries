package com.kw.starter.common.service.log

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class HttpLoggingService {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val mapper = jacksonObjectMapper().registerModule(JavaTimeModule())
    private val prettyPrinter = mapper.writerWithDefaultPrettyPrinter()

    fun displayReq(
        httpRequest: HttpServletRequest,
        requestBody: Any?,
    ) {
        val parameters =
            httpRequest.parameterNames
                .toList()
                .associateWith {
                    httpRequest.getParameterValues(it).joinToString(separator = ",")
                }.let {
                    prettyPrinter.writeValueAsString(it)
                } ?: "None"

        val headers =
            httpRequest.headerNames
                .toList()
                .associateWith {
                    httpRequest.getHeaders(it).toList().joinToString(separator = ",")
                }.let {
                    prettyPrinter.writeValueAsString(it)
                } ?: "None"

        val body = mapper.writeValueAsString(requestBody) ?: "None"

        logger.info(
            """
                |:: HTTP REQUEST :: {} {}
                |:. Parameters = [{}]
                |:. Request Headers = [{}]
                |:. Request Body = [{}]
            """.trimMargin(),
            httpRequest.method,
            httpRequest.requestURI,
            parameters,
            headers,
            body,
        )
    }

    fun displayResp(
        httpRequest: HttpServletRequest,
        httpResponse: HttpServletResponse,
        responseBody: Any?,
    ) {
        val headers =
            httpResponse.headerNames
                .toList()
                .associateWith {
                    httpResponse.getHeaders(it).toList().joinToString(separator = ",")
                }.let {
                    prettyPrinter.writeValueAsString(it)
                } ?: "None"

        val body = mapper.writeValueAsString(responseBody) ?: "None"

        logger.info(
            """
                |:: HTTP RESPONSE :: {} {} [{}]
                |:. Response Headers = [{}]
                |:. Response Body = [{}]
            """.trimMargin(),
            httpRequest.method,
            httpRequest.requestURI,
            httpResponse.status,
            headers,
            body,
        )
    }
}
