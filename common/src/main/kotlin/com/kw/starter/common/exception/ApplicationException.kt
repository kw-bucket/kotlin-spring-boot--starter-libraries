package com.kw.starter.common.exception

import com.kw.starter.common.constant.ApiOutputStatus
import org.springframework.http.HttpStatus

class ApplicationException(
    val httpStatus: HttpStatus,
    val apiOutputStatus: ApiOutputStatus,
    val description: String? = null,
    override val cause: Throwable? = null,
) : RuntimeException(cause)
