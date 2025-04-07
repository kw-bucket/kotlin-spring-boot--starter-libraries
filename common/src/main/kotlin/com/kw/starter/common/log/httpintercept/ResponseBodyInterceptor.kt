package com.kw.starter.common.log.httpintercept

import com.kw.starter.common.service.log.HttpLoggingService
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.http.server.ServletServerHttpResponse
import org.springframework.util.AntPathMatcher
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

@ControllerAdvice
class ResponseBodyInterceptor(
    private val httpLoggingService: HttpLoggingService,
) : ResponseBodyAdvice<Any> {
    override fun supports(
        returnType: MethodParameter,
        converterType: Class<out HttpMessageConverter<*>>,
    ): Boolean = true

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse,
    ): Any? {
        val httpRequest = (request as ServletServerHttpRequest).servletRequest

        val isExclusionPath = LogInterceptor.exclusionPaths.any { AntPathMatcher().match(it, httpRequest.servletPath) }
        if (!isExclusionPath) {
            httpLoggingService.displayResp(
                httpRequest = httpRequest,
                httpResponse = (response as ServletServerHttpResponse).servletResponse,
                responseBody = body,
            )
        }

        return body
    }
}
