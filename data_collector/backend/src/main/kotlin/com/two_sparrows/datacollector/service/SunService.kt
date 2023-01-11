package com.two_sparrows.datacollector.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.two_sparrows.datacollector.model.SunSchedule
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Service
class SunService(private val externalSunService: ExternalSunService) {

    private var sunSchedule: SunSchedule? = null
    private var currDate: LocalDate? = null

    private fun updateActualInfo() {
        val sunJson = externalSunService.requestSunData()
        val objectMapper = ObjectMapper()
        val jsonSun = objectMapper.readTree(sunJson).get("results")

        val sunset = jsonSun.get("sunset")
        val sunrise = jsonSun.get("sunrise")

        if (sunset == null || sunrise == null || !sunset.isTextual || !sunrise.isTextual) {
            return
        }
        sunSchedule = SunSchedule(convertStringToLocalTime(sunset.asText()), convertStringToLocalTime(sunrise.asText()))
        currDate = LocalDate.now()
    }

    private fun convertStringToLocalTime(stringDate: String): LocalTime {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val dt = LocalDateTime.parse(stringDate, formatter)
        return dt.toLocalTime()
    }

    fun getCurrentSunData(): SunSchedule {
        if (currDate == null || currDate != LocalDate.now()) {
            updateActualInfo()
        }
        return sunSchedule!!
    }
}