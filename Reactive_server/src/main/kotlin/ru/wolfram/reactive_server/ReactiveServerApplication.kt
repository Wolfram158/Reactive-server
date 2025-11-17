package ru.wolfram.reactive_server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ReactiveServerApplication

fun main(args: Array<String>) {
    runApplication<ReactiveServerApplication>(*args)
}
