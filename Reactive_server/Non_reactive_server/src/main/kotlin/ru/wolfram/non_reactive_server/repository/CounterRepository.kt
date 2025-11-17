package ru.wolfram.non_reactive_server.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ru.wolfram.non_reactive_server.entity.Counter

@Repository
interface CounterRepository : JpaRepository<Counter, Long> {
    @Query("update counter set value = value + 1 where name = :name", nativeQuery = true)
    @Modifying
    fun increment(name: String): Int

    @Query("update counter set value = value - 1 where name = :name", nativeQuery = true)
    @Modifying
    fun decrement(name: String): Int

    fun findByName(name: String): Counter?

    @Query("insert into counter (name, value) values (:name, 0) on conflict do nothing", nativeQuery = true)
    @Modifying
    fun create(name: String): Int
}