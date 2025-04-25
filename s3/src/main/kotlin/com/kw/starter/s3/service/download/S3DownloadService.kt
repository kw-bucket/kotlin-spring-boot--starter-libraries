package com.kw.starter.s3.service.download

import com.amazonaws.AmazonServiceException
import com.amazonaws.SdkClientException
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GetObjectRequest
import com.kw.starter.s3.dto.download.S3DownloadRequest
import com.kw.starter.s3.dto.download.S3DownloadResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class S3DownloadService(private val s3Client: AmazonS3) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun downloadFile(downloadRequest: S3DownloadRequest): S3DownloadResult =
        try {
            s3Client.getObject(
                GetObjectRequest(downloadRequest.bucket, downloadRequest.key),
                downloadRequest.location,
            )
        } catch (ex: AmazonServiceException) {
            logger.error(
                """
                AWS S3 Download Error:
                    - Bucket: {}
                    - Key: {}
                    - Description: {}
                """.trimIndent(),
                downloadRequest.bucket,
                downloadRequest.key,
                ex.errorMessage,
            )
            null
        } catch (ex: SdkClientException) {
            logger.error(ex.message ?: "Fatal Error: SdkClientException!")
            null
        }.let {
            S3DownloadResult(bucket = downloadRequest.bucket, key = downloadRequest.key, isSuccess = it != null)
        }
}
