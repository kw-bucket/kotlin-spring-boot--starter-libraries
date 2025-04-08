package com.kw.starter.s3.dto.download

import java.io.File

data class S3DownloadRequest(val bucket: String, val key: String, val location: File)
