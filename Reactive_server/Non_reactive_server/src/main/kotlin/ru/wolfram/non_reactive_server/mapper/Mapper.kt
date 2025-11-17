package ru.wolfram.non_reactive_server.mapper

import ru.wolfram.non_reactive_server.dto.CounterDto
import ru.wolfram.non_reactive_server.entity.Counter

fun Counter.toCounterDto() = CounterDto.Counter(
    name = name,
    value = value
)