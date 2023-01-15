package com.two_sparrows.datacollector.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "laser")
data class LaserConfiguration(
        val defaultLaserValue: Double,
        val acceptableDifference: Double
)