package com.kw.starter.common.manager

import com.kw.starter.common.config.properties.ThreadPoolProperty
import com.kw.starter.common.decorator.MdcTaskDecorator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

object ThreadPoolManager {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun initThreadPoolTaskExecutor(
        property: ThreadPoolProperty,
        log: Boolean = false,
    ): ThreadPoolTaskExecutor =
        ThreadPoolTaskExecutor()
            .apply {
                setThreadNamePrefix(property.threadNamePrefix)

                queueCapacity = property.capacity
                keepAliveSeconds = property.keepAliveTimeSeconds
                corePoolSize = property.pooling.core
                maxPoolSize = property.pooling.max

                setTaskDecorator(MdcTaskDecorator())
                setWaitForTasksToCompleteOnShutdown(property.waitTaskOnShutdown)
                setAwaitTerminationSeconds(property.awaitTerminationSeconds)
                afterPropertiesSet()
            }.also {
                if (log) {
                    logger.info(
                        """
                            |Configure Thread Pool Executor
                            |:. Prefix=[{}]
                            |:. Pool-Size=[Core: {} | Max: {}]
                            |:. Capacity=[{}]
                            |:. Keep-Alive-Seconds=[{}]
                            |:. Wait-Tasks-On-Shutdown=[{}]
                            |:. Await-Termination-Seconds=[{}]
                        """.trimMargin(),
                        it.threadNamePrefix,
                        it.corePoolSize,
                        it.maxPoolSize,
                        it.queueCapacity,
                        it.keepAliveSeconds,
                        property.waitTaskOnShutdown,
                        property.awaitTerminationSeconds,
                    )
                }
            }

    fun initFixedThreadPoolTaskExecutor(
        nThreads: Int,
        capacity: Int = Int.MAX_VALUE,
        keepAliveTimeSeconds: Int = 60,
        threadNamePrefix: String = "Thd-",
        waitTaskOnShutdown: Boolean = true,
        awaitTerminationSeconds: Int = 60,
        log: Boolean = false,
    ): ThreadPoolTaskExecutor =
        initThreadPoolTaskExecutor(
            property =
                ThreadPoolProperty(
                    threadNamePrefix = threadNamePrefix,
                    keepAliveTimeSeconds = keepAliveTimeSeconds,
                    capacity = capacity,
                    waitTaskOnShutdown = waitTaskOnShutdown,
                    awaitTerminationSeconds = awaitTerminationSeconds,
                    pooling =
                        ThreadPoolProperty.Pooling(
                            core = nThreads,
                            max = nThreads,
                        ),
                ),
            log = log,
        )

    fun initFixedThreadPoolExecutor(
        nThreads: Int,
        capacity: Int = Int.MAX_VALUE,
        keepAliveTimeSec: Int = 0,
    ): ThreadPoolExecutor =
        ThreadPoolExecutor(
            nThreads,
            nThreads,
            keepAliveTimeSec.seconds.inWholeMilliseconds,
            TimeUnit.MILLISECONDS,
            LinkedBlockingQueue(capacity),
        )
}
