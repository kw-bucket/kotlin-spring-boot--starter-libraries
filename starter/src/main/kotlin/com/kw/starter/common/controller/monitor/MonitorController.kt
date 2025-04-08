package com.kw.starter.common.controller.monitor

import org.springframework.boot.actuate.health.HealthComponent
import org.springframework.boot.actuate.health.HealthEndpoint
import org.springframework.boot.actuate.health.HttpCodeStatusMapper
import org.springframework.boot.actuate.info.InfoEndpoint
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class MonitorController(
    private val healthEndpoint: HealthEndpoint,
    private val infoEndpoint: InfoEndpoint,
    private val httpCodeStatusMapper: HttpCodeStatusMapper,
) {
    @GetMapping("/health")
    fun getHealth(): ResponseEntity<HealthComponent> =
        healthEndpoint.health().let { h ->
            ResponseEntity.status(httpCodeStatusMapper.getStatusCode(h.status)).body(h)
        }

    @GetMapping("/info")
    fun getInfo(): ResponseEntity<Map<String, Any>> = ResponseEntity.ok(infoEndpoint.info())
}
