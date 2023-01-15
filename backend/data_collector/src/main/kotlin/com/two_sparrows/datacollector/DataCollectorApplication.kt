package com.two_sparrows.datacollector

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DataCollectorApplication
fun main(args: Array<String>) {
	runApplication<DataCollectorApplication>(*args)
}
