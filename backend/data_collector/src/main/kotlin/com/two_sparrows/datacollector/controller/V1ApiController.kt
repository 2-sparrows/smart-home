package com.two_sparrows.datacollector.controller

import com.nikitaevg.datacollector.api.V1Api
import com.nikitaevg.datacollector.model.LaserValuesRequest
import com.nikitaevg.datacollector.model.LightStateRequest
import com.nikitaevg.datacollector.model.LightStateResponse
import com.nikitaevg.datacollector.model.RunTaskRequest
import com.two_sparrows.datacollector.service.LaserService
import com.two_sparrows.datacollector.service.LightService
import com.two_sparrows.datacollector.service.PhonesService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class V1ApiController(
        private val laserService: LaserService,
        private val phonesService: PhonesService,
        private val lightService: LightService
        ) : V1Api {

    override fun getExpectedLightsState(): ResponseEntity<LightStateResponse> {
        return ResponseEntity(LightStateResponse().lightState(lightService.getLightState()), HttpStatus.OK)
    }

    override fun submitLasersValue(laserValuesRequest: LaserValuesRequest): ResponseEntity<Void> {
        val hasNegativeNumbers = laserValuesRequest.values.stream().anyMatch { x ->  x < 0 }
        if (hasNegativeNumbers) {
            return ResponseEntity.badRequest().build()
        }
        laserService.updateLaserState(laserValuesRequest.values)
        //тут будет функция, которая отправляет сообщения в телегу
        return ResponseEntity.ok().build()
    }

    override fun runTask(runTaskRequest: RunTaskRequest?): ResponseEntity<Void> {
        phonesService.checkOnPhones()
        return ResponseEntity.ok().build()
    }

    override fun submitLightState(lightStateRequest: LightStateRequest): ResponseEntity<Void> {
        lightService.setManualState(lightStateRequest.lightState)
        return ResponseEntity.ok().build()
    }
}