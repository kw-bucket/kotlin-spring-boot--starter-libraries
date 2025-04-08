package com.kw.starter.s3.dto.upload

import java.io.File

data class S3UploadRequest(val bucket: String, val key: String, val file: File)
