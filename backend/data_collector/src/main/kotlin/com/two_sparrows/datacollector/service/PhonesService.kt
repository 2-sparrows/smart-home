package com.two_sparrows.datacollector.service

import com.two_sparrows.datacollector.config.CommonConfiguration
import com.two_sparrows.datacollector.config.PhoneConfiguration
import com.two_sparrows.datacollector.model.PhoneState
import org.springframework.stereotype.Service
import java.net.ConnectException
import java.net.InetAddress
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import javax.annotation.PostConstruct

@Service
class PhonesService(
        private val phoneConfiguration: PhoneConfiguration,
        private val commonConfiguration: CommonConfiguration,
        private val homeService: HomeService
) {

    private var phoneState: PhoneState = PhoneState.NotConnected;
    private val executor = Executors.newFixedThreadPool(phoneConfiguration.ips.size + 1)

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
        val secondsToWait = 600
        val result = AtomicBoolean(false)
        val tasks = ArrayList<Callable<Unit>>()
        for (ip in phoneConfiguration.ips) {
            val address = InetAddress.getByName(ip)
            tasks.add {
                try {
                    for (i in 0 until secondsToWait) {
                        if (address.isReachable(1000)) {
                            result.set(true)
                            return@add
                        }
                        if (result.get()) {
                            return@add
                        }
                    }
                    return@add
                } catch (e: ConnectException) {
                    return@add
                }
            }
        }
        executor.invokeAll(tasks)
        return result.get()
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