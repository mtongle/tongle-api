package com.tongle.tongleapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TongleApiApplication

fun main(args: Array<String>) {
    runApplication<TongleApiApplication>(*args)
}
