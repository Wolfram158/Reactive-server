package ru.wolfram.non_reactive_server.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import ru.wolfram.non_reactive_server.entity.Counter
import ru.wolfram.non_reactive_server.repository.CounterRepository

@Service
class CounterService(
    private val repository: CounterRepository
) {
    @Transactional
    fun increment(name: String): Counter? {
        return when (repository.increment(name)) {
            1 -> getByName(name)
            else -> null
        }
    }

    @Transactional
    fun decrement(name: String): Counter? {
        return when (repository.decrement(name)) {
            1 -> getByName(name)
            else -> null
        }
    }

    fun getByName(name: String): Counter? {
        return repository.findByName(name)
    }

    @Transactional
    fun createByName(name: String): Counter? {
        return when (repository.create(name)) {
            1 -> getByName(name)
            else -> null
        }
    }
}