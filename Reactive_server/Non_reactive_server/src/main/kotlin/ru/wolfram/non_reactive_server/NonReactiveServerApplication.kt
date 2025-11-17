package ru.wolfram.non_reactive_server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NonReactiveServerApplication

fun main(args: Array<String>) {
    runApplication<NonReactiveServerApplication>(*args)
}
