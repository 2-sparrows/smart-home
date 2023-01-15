package com.two_sparrows.datacollector.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "common")
data class CommonConfiguration(
        val env: String,
        val lat: String,
        val lng: String
        )