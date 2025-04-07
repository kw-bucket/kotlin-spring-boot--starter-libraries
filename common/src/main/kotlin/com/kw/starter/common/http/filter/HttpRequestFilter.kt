package com.kw.starter.common.http.filter

import com.kw.starter.common.http.constant.HeaderFields
import com.kw.starter.common.http.wrapper.ApplicationHttpRequestWrapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class HttpRequestFilter : OncePerRequestFilter() {
    @Value("\${starter.application-code:APP}")
    private val applicationCode: String = "APP"

    private val exclusionPaths = listOf("/health", "/info", "/metrics")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val isExclusionPath = exclusionPaths.any { request.requestURI.endsWith(it) }
        if (isExclusionPath) {
            filterChain.doFilter(request, response)
            return
        }

        val requestHeaderWrapper = ApplicationHttpRequestWrapper(request)

        val xCorrelationId = HeaderFields.X_CORRELATION_ID

        HeaderFields.TRACES.forEach { header ->
            request
                .getHeader(header)
                .let {
                    if (header == xCorrelationId && it.isNullOrBlank()) initCorrelationId() else it
                }?.also {
                    requestHeaderWrapper.putHeader(header, it)
                    response.setHeader(header, requestHeaderWrapper.getHeader(header))
                }
        }

        filterChain.doFilter(requestHeaderWrapper, response)
    }

    fun initCorrelationId(): String {
        val delimiter = "-"

        val randomId =
            UUID
                .randomUUID()
                .toString()
                .split(delimiter)
                .takeLast(2)
                .joinToString(separator = delimiter)

        return "$applicationCode-$randomId"
    }
}
