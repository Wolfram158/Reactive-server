package ru.wolfram.non_reactive_server.dto

sealed interface CounterDto {
    data class Counter(
        val name: String,
        val value: Long
    ) : CounterDto

    data class CounterNotFound(
        val message: String = "Counter not found"
    ) : CounterDto

    data class CounterAlreadyExists(
        val message: String = "Counter already exists"
    ) : CounterDto
}