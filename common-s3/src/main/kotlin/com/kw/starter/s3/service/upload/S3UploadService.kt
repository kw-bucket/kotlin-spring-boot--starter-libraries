package com.kw.starter.s3.service.upload

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.PutObjectRequest
import com.kw.starter.s3.dto.upload.S3UploadRequest
import com.kw.starter.s3.dto.upload.S3UploadResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class S3UploadService(private val s3Client: AmazonS3) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun uploadFiles(
        requests: List<S3UploadRequest>,
        concurrent: Int = requests.size,
    ): List<S3UploadResult> {
        val semaphore = Semaphore(concurrent)
        val mdcContext = MDCContext()

        return runBlocking(mdcContext) {
            requests.map {
                    request ->
                async(Dispatchers.IO) { semaphore.withPermit { uploadFile(request) } }
            }.awaitAll()
        }.also {
            logger.info("AWS S3 Upload Bulk __ Done!")
        }
    }

    fun uploadFile(uploadRequest: S3UploadRequest): S3UploadResult {
        val putObjectRequest = PutObjectRequest(uploadRequest.bucket, uploadRequest.key, uploadRequest.file)

        return try {
            s3Client.putObject(putObjectRequest)
        } catch (ex: AmazonServiceException) {
            logger.error(ex.errorMessage)
            null
        }.let {
            S3UploadResult(
                bucket = uploadRequest.bucket,
                key = uploadRequest.key,
                filename = uploadRequest.file.name,
                isSuccess = it != null,
            )
        }.also {
            logger.debug(
                """
                AWS S3 Upload File:
                    - Bucket: {}
                    - Key: {}
                    - Successful: {}
                """.trimIndent(),
                uploadRequest.bucket,
                uploadRequest.key,
                it.isSuccess,
            )
        }
    }
}
