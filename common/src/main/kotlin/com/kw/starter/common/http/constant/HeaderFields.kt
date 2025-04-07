package com.kw.starter.common.http.constant

object HeaderFields {
    const val CORRELATIONID = "correlationid"
    const val X_CORRELATION_ID = "x-correlation-id"
    const val TRACE_ID = "x-b3-traceid"
    const val SPAN_ID = "x-b3-spanid"
    const val SPAN_EXPORT = "x-span-export"
    const val X_ACCESS_KEY = "x-access-key"

    val TRACES = listOf(X_CORRELATION_ID, TRACE_ID, SPAN_ID, SPAN_EXPORT)
}
