package ru.wolfram.reactive_server.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import ru.wolfram.reactive_server.dto.CounterDto
import ru.wolfram.reactive_server.mapper.toCounterDto
import ru.wolfram.reactive_server.service.CounterService

@RestController
@RequestMapping("/api/v1")
class CounterController {
    @Autowired
    private lateinit var service: CounterService

    @PostMapping("/create-counter")
    fun createCounter(@RequestParam(required = true) name: String): Mono<CounterDto> {
        return service.createByName(name)
            .map { it.toCounterDto() }
            .switchIfEmpty(Mono.just(CounterDto.CounterAlreadyExists()))
    }

    @PostMapping("/increment-counter")
    fun incrementCounter(@RequestParam(required = true) name: String): Mono<CounterDto> {
        return service.increment(name)
            .map { it.toCounterDto() }
            .switchIfEmpty(Mono.just(CounterDto.CounterNotFound()))
    }

    @PostMapping("/decrement-counter")
    fun decrementCounter(@RequestParam(required = true) name: String): Mono<CounterDto> {
        return service.decrement(name)
            .map { it.toCounterDto() }
            .switchIfEmpty(Mono.just(CounterDto.CounterNotFound()))
    }

    @GetMapping("/get-counter")
    fun getCounter(@RequestParam(required = true) name: String): Mono<CounterDto> {
        return service.getByName(name)
            .map { it.toCounterDto() }
            .switchIfEmpty(Mono.just(CounterDto.CounterNotFound()))
    }
}