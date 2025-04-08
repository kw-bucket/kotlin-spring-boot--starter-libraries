package com.kw.starter.s3.dto.download

data class S3DownloadResult(val bucket: String, val key: String, val isSuccess: Boolean)
