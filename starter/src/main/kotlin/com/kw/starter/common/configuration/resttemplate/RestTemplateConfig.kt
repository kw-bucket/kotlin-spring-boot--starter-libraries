package com.kw.starter.common.configuration.resttemplate

import org.apache.hc.client5.http.classic.HttpClient
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import java.util.concurrent.TimeUnit

@Configuration
class RestTemplateConfig {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Value("\${starter.http-client.connection-request-timeout-sec:30}")
    private val connectionRequestTimeoutSec: Long = 30

    @Value("\${starter.http-client.response-timeout-sec:30}")
    private val responseTimeoutSec: Long = 30

    @Value("\${starter.http-client.pooling.connection-limit:100}")
    private val connectionLimit: Int = 100

    @Value("\${starter.http-client.pooling.max-connection-per-route:50}")
    private val maxConnectionPerRoute: Int = 50

    @Bean
    fun connectionManager(): PoolingHttpClientConnectionManager =
        PoolingHttpClientConnectionManager()
            .apply {
                maxTotal = connectionLimit
                defaultMaxPerRoute = maxConnectionPerRoute
            }.also {
                logger.debug(
                    """
                    Http Pooling Connection Config:
                        - Max-Connections-In-Pool[{}]
                        - Max-Connections-Per-Route[{}]
                    """.trimIndent(),
                    it.maxTotal,
                    it.defaultMaxPerRoute,
                )
            }

    @Bean
    fun requestConfig(): RequestConfig =
        RequestConfig
            .custom()
            .apply {
                setConnectionRequestTimeout(connectionRequestTimeoutSec, TimeUnit.SECONDS)
                setResponseTimeout(responseTimeoutSec, TimeUnit.SECONDS)
            }.build()
            .also {
                logger.debug(
                    """
                    Http Request Config:
                        - Connection-Request-Timeout[{}]
                        - Response-Timeout[{}]
                    """.trimIndent(),
                    it.connectionRequestTimeout,
                    it.responseTimeout,
                )
            }

    @Bean
    fun httpClient(
        connectionManager: PoolingHttpClientConnectionManager,
        requestConfig: RequestConfig,
    ): CloseableHttpClient =
        HttpClientBuilder
            .create()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .build()

    @Bean
    fun clientHttpRequestFactory(httpClient: HttpClient): HttpComponentsClientHttpRequestFactory =
        HttpComponentsClientHttpRequestFactory()
            .apply {
                setHttpClient(httpClient)
            }

    @Bean
    fun restTemplate(clientHttpRequestFactory: HttpComponentsClientHttpRequestFactory): RestTemplate =
        RestTemplate(clientHttpRequestFactory)
}
