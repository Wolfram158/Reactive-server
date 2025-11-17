package ru.wolfram.reactive_server.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import ru.wolfram.non_reactive_server.entity.Counter
import ru.wolfram.reactive_server.repository.CounterRepository

@Service
class CounterService(
    private val repository: CounterRepository
) {
    @Transactional
    fun increment(name: String): Mono<Counter> {
        return repository.increment(name)
            .flatMap {
                if (it == 1) {
                    getByName(name)
                } else {
                    Mono.empty()
                }
            }
    }

    @Transactional
    fun decrement(name: String): Mono<Counter> {
        return repository.decrement(name)
            .flatMap {
                if (it == 1) {
                    getByName(name)
                } else {
                    Mono.empty()
                }
            }
    }

    fun getByName(name: String): Mono<Counter> {
        return repository.findByName(name)
    }

    @Transactional
    fun createByName(name: String): Mono<Counter> {
        return repository.create(name)
            .flatMap {
                if (it == 1) {
                    getByName(name)
                } else {
                    Mono.empty()
                }
            }
    }
}