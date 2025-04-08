package com.kw.starter.common.configuration.application

data class ThreadPoolConfig(
    val threadNamePrefix: String = "Thd-",
    val keepAliveTimeSec: Int = 0,
    val capacity: Int = Int.MAX_VALUE,
    val pooling: Pooling = Pooling(),
) {
    data class Pooling(
        val core: Int = 1,
        val max: Int = Int.MAX_VALUE,
    )
}
