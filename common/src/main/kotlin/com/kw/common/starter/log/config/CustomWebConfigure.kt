package com.kw.common.starter.log.config

import com.kw.common.starter.log.httpintercept.LogInterceptor
import org.springframework.stereotype.Component
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Component
class CustomWebConfigure(private val logInterceptor: LogInterceptor) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(logInterceptor).excludePathPatterns(LogInterceptor.exclusionPaths)
    }
}
