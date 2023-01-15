package com.two_sparrows.datacollector.service

import com.nikitaevg.datacollector.model.LightState
import com.two_sparrows.datacollector.model.DayPhase
import com.two_sparrows.datacollector.model.HomeState
import com.two_sparrows.datacollector.model.SunSchedule
import org.springframework.stereotype.Service
import java.time.LocalTime

@Service
class LightService(
        private val homeService: HomeService,
        private val sunService: SunService
) {

    private var dayPhase = DayPhase.DAY
    private var manualLightState: LightState? = null

    @Synchronized
    fun getLightState(): LightState {
        val sunSchedule = sunService.getCurrentSunData()
        val now = LocalTime.now()
        val currWorkPhase = getDayPhase(now, sunSchedule)
        if (currWorkPhase != dayPhase) {
            dayPhase = currWorkPhase
            manualLightState = null
        }
        if (homeService.getCurrentState() == HomeState.NobodyHome) {
            return LightState.OFF
        }
        return if (manualLightState != null) {
            manualLightState!!
        } else if (getDayPhase(now, sunSchedule) == DayPhase.DAY) {
            LightState.OFF
        } else {
            LightState.ON
        }
    }

    @Synchronized
    fun setManualState(manualState: LightState) {
        manualLightState = manualState
    }

    private fun getDayPhase(now: LocalTime, sunSchedule: SunSchedule): DayPhase {
        if (now.isAfter(sunSchedule.sunrise) && now.isBefore(sunSchedule.sunset.minusMinutes(30))) {
            return DayPhase.DAY
        }
        return DayPhase.NIGHT
    }
}