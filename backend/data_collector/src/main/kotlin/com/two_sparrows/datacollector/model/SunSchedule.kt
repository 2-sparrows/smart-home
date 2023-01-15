package com.two_sparrows.datacollector.model

import java.time.LocalTime

data class SunSchedule (
    val sunset: LocalTime,
    val sunrise: LocalTime
)