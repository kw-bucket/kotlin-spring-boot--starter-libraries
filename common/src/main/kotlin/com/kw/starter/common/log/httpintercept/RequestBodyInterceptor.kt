package com.kw.starter.common.log.httpintercept

import com.kw.starter.common.service.log.HttpLoggingService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.http.HttpInputMessage
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.util.AntPathMatcher
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter
import java.lang.reflect.Type

@ControllerAdvice
class RequestBodyInterceptor(
    private val httpLoggingService: HttpLoggingService,
    private val httpRequest: HttpServletRequest,
) : RequestBodyAdviceAdapter() {
    override fun afterBodyRead(
        body: Any,
        inputMessage: HttpInputMessage,
        parameter: MethodParameter,
        targetType: Type,
        converterType: Class<out HttpMessageConverter<*>>,
    ): Any {
        val isExclusionPath = LogInterceptor.exclusionPaths.any { AntPathMatcher().match(it, httpRequest.servletPath) }
        if (!isExclusionPath) {
            httpLoggingService.displayReq(httpRequest = httpRequest, requestBody = body)
        }

        return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType)
    }

    override fun supports(
        methodParameter: MethodParameter,
        targetType: Type,
        converterType: Class<out HttpMessageConverter<*>>,
    ): Boolean = true
}
