package com.kw.starter.mailing.dto.downstream

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.core.io.FileSystemResource
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.io.File

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class EmailRequest(
    val from: String,
    val to: String,
    val subject: String,
    val body: String,
    val cc: String? = null,
    val bcc: String? = null,
    val files: List<File>? = null,
)

fun EmailRequest.asMap(): MultiValueMap<String, Any> {
    val map: MultiValueMap<String, Any> = LinkedMultiValueMap()
    map.add("email_from", this.from)
    map.add("emails_to", this.to)
    map.add("subject", this.subject)
    map.add("body", this.body)

    this.cc?.takeIf {
        it.isNotBlank()
    }?.also {
        map.add("emails_cc", it)
    }

    this.bcc?.takeIf {
        it.isNotBlank()
    }?.also {
        map.add("emails_bcc", it)
    }

    this.files?.map {
        FileSystemResource(it)
    }?.takeIf {
        it.isNotEmpty()
    }?.also {
        map.addAll("files", it)
    }

    return map
}
