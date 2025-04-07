package com.kw.starter.mailing.extension

import com.kw.starter.mailing.config.EmailConfig
import com.kw.starter.mailing.dto.downstream.EmailRequest
import java.io.File

fun EmailConfig.toRequest(
    subject: String? = null,
    body: String,
    attachments: List<File>? = null,
): EmailRequest =
    EmailRequest(
        subject = subject ?: requireNotNull(this.subject) { "You must tell readers what your email is about." },
        from = requireNotNull(this.from) { "You must represent identity of sender" },
        to = requireNotNull(this.to) { "You must specify recipient(s) of email" },
        cc = this.cc,
        bcc = this.bcc,
        body = body,
        files = attachments,
    )
