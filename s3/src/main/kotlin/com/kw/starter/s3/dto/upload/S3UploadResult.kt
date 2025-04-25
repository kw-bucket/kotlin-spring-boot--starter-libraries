package com.kw.starter.s3.dto.upload

data class S3UploadResult(val bucket: String, val key: String, val filename: String, val isSuccess: Boolean)
