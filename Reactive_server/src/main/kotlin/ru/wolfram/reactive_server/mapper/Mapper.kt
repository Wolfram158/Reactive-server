package ru.wolfram.reactive_server.mapper

import ru.wolfram.non_reactive_server.entity.Counter
import ru.wolfram.reactive_server.dto.CounterDto

fun Counter.toCounterDto(): CounterDto = CounterDto.Counter(
    name = name,
    value = value
)