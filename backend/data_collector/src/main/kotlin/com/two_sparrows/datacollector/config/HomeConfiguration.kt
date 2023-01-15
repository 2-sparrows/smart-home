package com.two_sparrows.datacollector.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "home")
data class HomeConfiguration(
        val maxTimeForComingMs: Long
)