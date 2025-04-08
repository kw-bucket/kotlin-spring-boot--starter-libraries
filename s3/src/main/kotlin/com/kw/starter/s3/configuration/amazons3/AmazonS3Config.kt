package com.kw.starter.s3.configuration.amazons3

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AmazonS3Config {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Value("\${cloud.aws.credentials.access-key:#{null}}")
    private val awsAccessKey: String? = null

    @Value("\${cloud.aws.credentials.secret-key:#{null}}")
    private val awsSecretKey: String? = null

    @Value("\${cloud.aws.region.static:#{null}}")
    private val region: String? = null

    @Bean
    fun s3Client(): AmazonS3 =
        Triple(region, awsAccessKey, awsSecretKey).also {
            logger.debug(
                """
                AWS S3 Configuration:
                    - Region[{}]
                    - Access Key [{}]
                    - Secret Key [{}]
                """.trimIndent(),
                region,
                awsAccessKey?.let { "Shhh!" },
                awsSecretKey?.let { "Shhh!" },
            )
        }.takeIf {
            !it.first.isNullOrBlank() && !it.second.isNullOrBlank() && !it.third.isNullOrBlank()
        }?.let {
            staticS3Client(region!!, awsAccessKey!!, awsSecretKey!!)
        } ?: defaultS3Client()

    fun staticS3Client(
        region: String,
        awsAccessKey: String,
        awsSecretKey: String,
    ): AmazonS3 =
        AmazonS3ClientBuilder.standard()
            .withRegion(region)
            .withCredentials(AWSStaticCredentialsProvider(BasicAWSCredentials(awsAccessKey, awsSecretKey)))
            .build()
            .also { logger.debug("Initiate S3 Client Using Static Credentials Provider") }

    fun defaultS3Client(): AmazonS3 =
        AmazonS3ClientBuilder.standard()
            .withCredentials(DefaultAWSCredentialsProviderChain())
            .build()
            .also { logger.debug("Initiate S3 Client Using Default Credentials Provider Chain") }
}
