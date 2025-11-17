package ru.wolfram.reactive_server.repository

import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import ru.wolfram.non_reactive_server.entity.Counter

@Repository
interface CounterRepository : R2dbcRepository<Counter, Long> {
    @Query("update counter set value = value + 1 where name = :name")
    @Modifying
    fun increment(@Param("name") name: String): Mono<Int>

    @Query("update counter set value = value - 1 where name = :name")
    @Modifying
    fun decrement(@Param("name") name: String): Mono<Int>

    fun findByName(@Param("name") name: String): Mono<Counter>

    @Query("insert into counter (name, value) values (:name, 0) on conflict do nothing")
    @Modifying
    fun create(@Param("name") name: String): Mono<Int>
}