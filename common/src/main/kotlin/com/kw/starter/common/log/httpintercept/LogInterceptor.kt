package com.kw.starter.common.log.httpintercept

import com.kw.starter.common.http.constant.HeaderFields
import com.kw.starter.common.log.constant.LogbackFields
import com.kw.starter.common.service.log.HttpLoggingService
import jakarta.servlet.DispatcherType
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class LogInterceptor(
    private val httpLoggingService: HttpLoggingService,
) : HandlerInterceptor {
    companion object {
        val exclusionPaths =
            listOf(
                "/health",
                "/info",
                "/metrics",
                "/prometheus",
                "/actuator**",
                "/swagger-ui.html",
                "/swagger-resources/**",
                "/webjars/**",
                "/v2/api-docs",
                "/configuration/**",
            )
    }

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        MDC.put(LogbackFields.CORRELATION_ID, request.getHeader(HeaderFields.X_CORRELATION_ID))
        MDC.put(LogbackFields.TRACE_ID, request.getHeader(HeaderFields.TRACE_ID))
        MDC.put(LogbackFields.SPAN_ID, request.getHeader(HeaderFields.SPAN_ID))
        MDC.put(LogbackFields.SPAN_EXPORT, request.getHeader(HeaderFields.SPAN_EXPORT))

        val isDispatcherTypeRequest = request.dispatcherType == DispatcherType.REQUEST
        val isMethodGet = HttpMethod.valueOf(request.method) == HttpMethod.GET

        if (isDispatcherTypeRequest && isMethodGet) {
            httpLoggingService.displayReq(httpRequest = request, requestBody = null)
        }

        return true
    }
}
