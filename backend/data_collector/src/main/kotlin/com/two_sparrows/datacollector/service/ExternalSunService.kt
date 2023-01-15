package com.two_sparrows.datacollector.service

import com.two_sparrows.datacollector.config.CommonConfiguration
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Service
class ExternalSunService(private val commonConfiguration: CommonConfiguration) {
    fun requestSunData() : String {
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("https://api.sunrise-sunset.org/json?lat=%s&lng=%s&formatted=0",
                        commonConfiguration.lat, commonConfiguration.lng)))
                .build()

        // TODO: process sending errors, 5xx etc
        return client.send(request, HttpResponse.BodyHandlers.ofString()).body()
    }
}