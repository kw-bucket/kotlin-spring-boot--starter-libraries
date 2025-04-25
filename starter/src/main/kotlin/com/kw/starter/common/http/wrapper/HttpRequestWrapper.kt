package com.kw.starter.common.http.wrapper

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import java.util.Collections
import java.util.Enumeration

class HttpRequestWrapper(
    request: HttpServletRequest,
) : HttpServletRequestWrapper(request) {
    private val customHeaders: MutableMap<String, String> = mutableMapOf()

    fun putHeader(
        name: String,
        value: String,
    ) {
        customHeaders[name] = value
    }

    override fun getHeader(name: String?): String? {
        val headerName: String? = name?.lowercase()
        val headerValue: String? = customHeaders[headerName]

        headerValue ?: return super.getHeader(headerName)

        return headerValue
    }

    override fun getHeaderNames(): Enumeration<String> =
        Collections.enumeration(super.getHeaderNames().toList() + customHeaders.keys)

    override fun getHeaders(name: String?): Enumeration<String> {
        val headerName: String? = name?.lowercase()
        val headerValues = (super.getHeaders(headerName).toList() + customHeaders[headerName]).filterNotNull()

        return Collections.enumeration(headerValues)
    }
}
