package com.two_sparrows.datacollector.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "phone")
data class PhoneConfiguration(
        val phoneCheckJobPeriod: Long,
        val ips: List<String>
)