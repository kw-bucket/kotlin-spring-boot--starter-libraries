package com.kw.starter.mailing.service.notification

import com.kw.starter.common.extension.apiresponse.peekError
import com.kw.starter.common.extension.apiresponse.peekOutputStatus
import com.kw.starter.common.manager.ThreadPoolManager
import com.kw.starter.common.service.api.ApiResponse
import com.kw.starter.mailing.dto.downstream.EmailRequest
import com.kw.starter.mailing.service.api.NotificationApiService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class EmailNotificationService(private val notificationApiService: NotificationApiService) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun sendEmail(request: EmailRequest) = callEmailNotificationApi(request)

    fun sendEmailAsync(request: EmailRequest) {
        val pool =
            ThreadPoolManager.initFixedThreadPoolTaskExecutor(
                nThreads = 1,
                threadNamePrefix = "Thd-EmailAsync-",
            ).apply {
                setWaitForTasksToCompleteOnShutdown(true)
            }

        pool.execute {
            val result =
                when (val response = callEmailNotificationApi(request)) {
                    is ApiResponse.Success,
                    is ApiResponse.Failure,
                    -> {
                        response.peekOutputStatus()
                    }
                    is ApiResponse.Error -> response.peekError()
                }

            logger.info("Send asynchronous email notification - Subject[{}] - Result[{}]", request.subject, result)
        }

        pool.shutdown()
    }

    private fun callEmailNotificationApi(request: EmailRequest) = notificationApiService.sendEmail(request)
}
