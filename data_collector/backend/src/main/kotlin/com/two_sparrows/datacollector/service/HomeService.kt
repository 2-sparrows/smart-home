package com.two_sparrows.datacollector.service

import com.two_sparrows.datacollector.config.HomeConfiguration
import com.two_sparrows.datacollector.model.HomeState
import org.springframework.stereotype.Service
import java.util.*
import kotlin.concurrent.schedule

@Service
class HomeService(private val homeConfiguration: HomeConfiguration) {

    private var homeState: HomeState = HomeState.NobodyHome

    @Synchronized
    fun onLaserObstructed() {
        when (homeState) {
            HomeState.NobodyHome -> {
                homeState = HomeState.SomeoneIsComing
            }
            HomeState.SomeoneIsComing, HomeState.SomeoneAtHome -> {}
        }
    }

    @Synchronized
    fun onLaserNotObstructed() {
        if (homeState != HomeState.SomeoneIsComing) {
            return
        }
        Timer("RevertSomeoneIsComing", false).schedule(homeConfiguration.maxTimeForComingMs) {
            if (homeState == HomeState.SomeoneIsComing) {
                homeState = HomeState.NobodyHome
                //тут можно отправить алерт
            }
        }
    }

    @Synchronized
    fun getCurrentState(): HomeState {
        return homeState;
    }

    @Synchronized
    fun onPhoneConnected() {
        homeState = HomeState.SomeoneAtHome
    }

    @Synchronized
    fun onPhoneDisconnected() {
        if (homeState == HomeState.SomeoneAtHome) {
            homeState = HomeState.NobodyHome
        }
    }
}