package com.two_sparrows.datacollector.service

import com.two_sparrows.datacollector.config.LaserConfiguration
import com.two_sparrows.datacollector.model.LaserState
import org.springframework.stereotype.Service
import kotlin.math.abs

@Service
class LaserService(
        private val laserConfiguration: LaserConfiguration,
        private val homeService: HomeService) {

    private var laserState: LaserState = LaserState.NotObstructed

    fun updateLaserState(values: List<Double>) {
        for (currLaserValue in values) {
            if (abs(laserConfiguration.defaultLaserValue - currLaserValue) > laserConfiguration.acceptableDifference) {
                laserState = LaserState.Obstructed
                homeService.onLaserObstructed()
                return
            }
        }
        laserState = LaserState.NotObstructed
        homeService.onLaserNotObstructed()
    }
}