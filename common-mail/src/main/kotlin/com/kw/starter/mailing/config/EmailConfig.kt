package com.kw.starter.mailing.config

data class EmailConfig(
    val from: String? = null,
    val to: String? = null,
    val cc: String? = null,
    val bcc: String? = null,
    val subject: String? = null,
    val bodyTemplate: String? = null,
)
