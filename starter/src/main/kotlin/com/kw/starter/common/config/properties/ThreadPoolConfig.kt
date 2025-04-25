package com.kw.starter.common.config.properties

data class ThreadPoolProperty(
    val threadNamePrefix: String = "Thd-",
    val keepAliveTimeSeconds: Int = 60,
    val capacity: Int = Int.MAX_VALUE,
    val waitTaskOnShutdown: Boolean = true,
    val awaitTerminationSeconds: Int = 60,
    val pooling: Pooling = Pooling(),
) {
    data class Pooling(
        val core: Int = 1,
        val max: Int = Int.MAX_VALUE,
    )
}
