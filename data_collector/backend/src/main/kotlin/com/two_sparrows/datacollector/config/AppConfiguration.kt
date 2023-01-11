package com.two_sparrows.datacollector.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
        LaserConfiguration::class,
        PhoneConfiguration::class,
        HomeConfiguration::class,
        CommonConfiguration::class)
class AppConfiguration {

    @Bean
    fun apiConfiguration(): AppConfiguration {
        return AppConfiguration()
    }

}