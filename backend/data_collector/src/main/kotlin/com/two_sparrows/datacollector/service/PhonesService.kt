package com.two_sparrows.datacollector.service

import com.two_sparrows.datacollector.config.CommonConfiguration
import com.two_sparrows.datacollector.config.PhoneConfiguration
import com.two_sparrows.datacollector.model.PhoneState
import org.springframework.stereotype.Service
import java.net.ConnectException
import java.net.InetAddress
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Executors
import javax.annotation.PostConstruct

@Service
class PhonesService(
        private val phoneConfiguration: PhoneConfiguration,
        private val commonConfiguration: CommonConfiguration,
        private val homeService: HomeService
) {

    private var phoneState: PhoneState = PhoneState.NotConnected;

    @PostConstruct
    fun run() {
        if (commonConfiguration.env != "prod") {
            return
        }
        Thread {
            while (true) {
                try {
                    checkOnPhones()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                Thread.sleep(phoneConfiguration.phoneCheckJobPeriod)
            }
        }.start()
    }

    private fun hasConnectedPhones() : Boolean {
        val timeout = 10 * 60 * 1000
        val executor = Executors.newFixedThreadPool(phoneConfiguration.ips.size + 1)
        val completionService = ExecutorCompletionService<Boolean>(executor)
        for (ip in phoneConfiguration.ips) {
            val address = InetAddress.getByName(ip)
            completionService.submit {
                try {
                    return@submit address.isReachable(timeout)
                } catch (e: ConnectException) {
                    return@submit false
                }
            }
        }
        for (i in 0 until phoneConfiguration.ips.size) {
            val result = completionService.take().get()
            if (result) {
                executor.shutdown()
                return result
            }
        }
        return false
    }

    fun checkOnPhones() {
        val hasConnectedPhone = hasConnectedPhones()
        println("has connected phones = $hasConnectedPhone")
        if (hasConnectedPhone && phoneState == PhoneState.NotConnected) {
            phoneState = PhoneState.Connected
            homeService.onPhoneConnected()
            return
        }
        if (!hasConnectedPhone && phoneState == PhoneState.Connected) {
            phoneState = PhoneState.NotConnected
            homeService.onPhoneDisconnected()
        }
    }

}