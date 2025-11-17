package ru.wolfram.non_reactive_server.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.wolfram.non_reactive_server.dto.CounterDto
import ru.wolfram.non_reactive_server.mapper.toCounterDto
import ru.wolfram.non_reactive_server.service.CounterService

@RestController
@RequestMapping("/api/v1")
class CounterController {
    @Autowired
    private lateinit var service: CounterService

    @PostMapping("/create-counter")
    fun createCounter(@RequestParam(required = true) name: String): ResponseEntity<CounterDto> {
        return service.createByName(name)?.let {
            ResponseEntity.ok(it.toCounterDto())
        } ?: ResponseEntity.ok(CounterDto.CounterAlreadyExists())
    }

    @PostMapping("/increment-counter")
    fun incrementCounter(@RequestParam(required = true) name: String): ResponseEntity<CounterDto> {
        return service.increment(name)?.let {
            ResponseEntity.ok(it.toCounterDto())
        } ?: ResponseEntity.ok(CounterDto.CounterNotFound())
    }

    @PostMapping("/decrement-counter")
    fun decrementCounter(@RequestParam(required = true) name: String): ResponseEntity<CounterDto> {
        return service.decrement(name)?.let {
            ResponseEntity.ok(it.toCounterDto())
        } ?: ResponseEntity.ok(CounterDto.CounterNotFound())
    }

    @GetMapping("/get-counter")
    fun getCounter(@RequestParam(required = true) name: String): ResponseEntity<CounterDto> {
        return service.getByName(name)?.let {
            ResponseEntity.ok(it.toCounterDto())
        } ?: ResponseEntity.ok(CounterDto.CounterNotFound())
    }
}