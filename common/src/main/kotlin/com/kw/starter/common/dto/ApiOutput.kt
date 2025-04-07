package com.kw.starter.common.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.kw.starter.common.constant.ApiOutputStatus

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiOutput<T : Any>(
    val status: Status,
    val data: T?,
) {
    companion object {
        fun <T : Any> fromStatus(
            apiOutputStatus: ApiOutputStatus,
            description: String? = null,
            data: T? = null,
        ): ApiOutput<T> =
            ApiOutput(
                status =
                    Status(
                        code = apiOutputStatus.code,
                        message = apiOutputStatus.message,
                        description = description ?: apiOutputStatus.description,
                    ),
                data = data,
            )

        fun <T : Any> fromCustomStatus(
            code: String,
            message: String,
            description: String? = null,
            data: T? = null,
        ): ApiOutput<T> =
            ApiOutput(
                status = Status(code = code, message = message, description = description),
                data = data,
            )
    }

    data class Status(
        val code: String,
        val message: String,
        val description: String? = null,
    )
}
