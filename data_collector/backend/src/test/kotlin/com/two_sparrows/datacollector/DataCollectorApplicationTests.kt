package com.two_sparrows.datacollector

import com.nikitaevg.datacollector.model.LaserValuesRequest
import com.nikitaevg.datacollector.model.LightState
import com.nikitaevg.datacollector.model.LightStateRequest
import com.nikitaevg.datacollector.model.LightStateResponse
import com.nikitaevg.datacollector.model.RunTaskRequest
import com.two_sparrows.datacollector.controller.V1ApiController
import com.two_sparrows.datacollector.service.ExternalSunService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import java.net.InetAddress
import java.time.LocalDate
import java.time.LocalTime

@SpringBootTest
@TestPropertySource("classpath:test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DataCollectorApplicationTests {

    @Autowired
    private lateinit var v1ApiController: V1ApiController

    @MockBean
    private lateinit var externalSunService: ExternalSunService

    @Test
    fun testLightTurnsOffWhenNobodyHome() {
        atDay().use {
            mockPingPhones(true).use {
                submitObstructedLaserValue()
                v1ApiController.runTask(RunTaskRequest())
            }
            checkLightsOff()
            v1ApiController.submitLightState(LightStateRequest().lightState(LightState.ON))
            checkLightsOn()
            mockPingPhones(false).use {
                submitObstructedLaserValue()
                v1ApiController.runTask(RunTaskRequest())
            }
            checkLightsOff()
        }
    }

    @Test
    fun testManualHandling() {
        atNight().use {
            mockPingPhones(true).use {
                submitObstructedLaserValue()
                v1ApiController.runTask(RunTaskRequest())
            }
            checkLightsOn()
            v1ApiController.submitLightState(LightStateRequest().lightState(LightState.OFF))
            checkLightsOff()
        }
    }

    @Test
    fun testSubmitLaserValue() {
        atNight().use {
            assertEquals(ResponseEntity.ok().build<Void>(),
                    v1ApiController.submitLasersValue(LaserValuesRequest().values(listOf())))
            checkLightsOff()

            assertEquals(ResponseEntity.ok().build<Void>(),
                    v1ApiController.submitLasersValue(LaserValuesRequest().values(listOf(310.0, 299.0, 317.0))))
            checkLightsOff()

            assertEquals(ResponseEntity.ok().build<Void>(),
                    v1ApiController.submitLasersValue(LaserValuesRequest().values(listOf(250.0, 299.0, 317.0))))
            checkLightsOn()

            val actual = v1ApiController.submitLasersValue(LaserValuesRequest().values(listOf(-250.0, 299.0, -317.0)))
            assertEquals(ResponseEntity.badRequest().build<Void>(), actual)
        }
    }

    @Test
    fun lightOnWithLaserAndPhone() {
        atNight().use {
            mockPingPhones(true).use {
                submitObstructedLaserValue()
                v1ApiController.runTask(RunTaskRequest())
            }
            checkLightsOn()
        }
    }

    @Test
    fun lightOnWithLaser() {
        atNight().use {
            mockPingPhones(false).use {
                submitObstructedLaserValue()
                v1ApiController.runTask(RunTaskRequest())
            }
            checkLightsOn()
        }
    }

    @Test
    fun lightOnWithPhone() {
        atNight().use {
            mockPingPhones(true).use {
                v1ApiController.runTask(RunTaskRequest())
            }
            checkLightsOn()
        }
    }

    @Test
    fun lightOff() {
        atNight().use {
            mockPingPhones(false).use {
                submitNotObstructedLaserValue()
                v1ApiController.runTask(RunTaskRequest())
            }
            checkLightsOff()
        }
    }

    @Test
    fun defaultState() {
        atNight().use {
            submitNotObstructedLaserValue()
            checkLightsOff()
        }
    }

    @Test
    fun revertToNobodyHome() {
        atNight().use {
            mockPingPhones(true).use {
                v1ApiController.runTask(RunTaskRequest())
            }
            checkLightsOn()
            mockPingPhones(false).use {
                v1ApiController.runTask(RunTaskRequest())
            }
            checkLightsOff()
        }
    }

    @Test
    fun checkLightOffAfterTimeout() {
        atNight().use {
            submitObstructedLaserValue()
            submitNotObstructedLaserValue()
            checkLightsOn()
            Thread.sleep(100)
            checkLightsOff()
        }
    }

    private fun mockPingPhones(isReachable: Boolean): MockedStatic<InetAddress> {
        val inetAddressMock = Mockito.mockStatic(InetAddress::class.java)
        val address = Mockito.mock(InetAddress::class.java)
        Mockito.`when`(address.isReachable(any())).thenReturn(isReachable)
        inetAddressMock.`when`<Any> { InetAddress.getByName(any()) }.thenReturn(address, address, address)
        return inetAddressMock
    }

    private fun mockTime(time: LocalTime): MockedStatic<LocalTime> {
        val currentDate = LocalDate.now()
        Mockito.`when`(externalSunService.requestSunData()).thenReturn("{\n" +
                "  \"results\": {\n" +
                "    \"sunrise\": \"${currentDate}T09:00:00+00:00\",\n" +
                "    \"sunset\": \"${currentDate}T18:00:00+00:00\"\n" +
                "  }\n" +
                "}")
        val timeMock = Mockito.mockStatic(LocalTime::class.java, Mockito.CALLS_REAL_METHODS)
        timeMock.`when`<Any>{ LocalTime.now() }.thenReturn(time)
        return timeMock
    }

    private fun atDay(): MockedStatic<LocalTime> {
        return mockTime(LocalTime.of(15, 0,0))
    }

    private fun atNight(): MockedStatic<LocalTime> {
        return mockTime(LocalTime.of(21, 0,0))
    }

    private fun submitObstructedLaserValue() {
        v1ApiController.submitLasersValue(LaserValuesRequest().values(listOf(250.0, 200.0)))
    }

    private fun submitNotObstructedLaserValue() {
        v1ApiController.submitLasersValue(LaserValuesRequest().values(listOf(300.0)))
    }

    private fun checkLightsOn() {
        for (i in 0..5) {
            assertEquals(ResponseEntity(LightStateResponse().lightState(LightState.ON), HttpStatus.OK),
                    v1ApiController.expectedLightsState)
        }
    }

    private fun checkLightsOff() {
        for (i in 0..5) {
            assertEquals(ResponseEntity(LightStateResponse().lightState(LightState.OFF), HttpStatus.OK),
                    v1ApiController.expectedLightsState)
        }
    }

}
