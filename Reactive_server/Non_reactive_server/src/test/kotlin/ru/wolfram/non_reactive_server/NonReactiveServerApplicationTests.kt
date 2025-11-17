package ru.wolfram.non_reactive_server

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import ru.wolfram.non_reactive_server.dto.CounterDto
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import kotlin.test.assertContains
import kotlin.test.assertEquals

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NonReactiveServerApplicationTests {
    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate

    @LocalServerPort
    private val port: Int = 0

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun prepare() {
        jdbcTemplate.execute("delete from counter")
    }

    companion object {
        @Container
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:18")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @Test
    fun `create counter and then increment it 10000 times concurrently`() {
        val executors = Executors.newFixedThreadPool(12)
        val name = "cnt1"
        val counter = testRestTemplate.postForObject(
            "http://localhost:$port/api/v1/create-counter?name=$name",
            null,
            CounterDto.Counter::class.java
        )

        assertEquals(0, counter.value)
        assertEquals(name, counter.name)

        val times = 10000
        val results = Collections.synchronizedList(ArrayList<String?>())
        val cdl = CountDownLatch(times)
        repeat(times) {
            executors.submit {
                results.add(
                    testRestTemplate.postForObject(
                        "http://localhost:$port/api/v1/increment-counter?name=$name",
                        null,
                        String::class.java
                    )
                )
                cdl.countDown()
            }
        }

        cdl.await()
        executors.shutdownNow()

        results.all { result ->
            assertNotNull(result)
            assertInstanceOf<String>(result)
            assertContains(result, "name")
            assertContains(result, "value")
            true
        }

        val end = testRestTemplate.getForObject(
            "http://localhost:$port/api/v1/get-counter?name=$name",
            CounterDto.Counter::class.java
        )

        assertEquals(end.value, times.toLong())
    }

    @Test
    fun `try to create counter with the same name 10000 times concurrently`() {
        val executors = Executors.newFixedThreadPool(12)
        val name = "cnt1"

        val times = 10000
        val results = Collections.synchronizedList(ArrayList<String?>())
        val cdl = CountDownLatch(times)
        repeat(times) {
            executors.submit {
                results.add(
                    testRestTemplate.postForObject(
                        "http://localhost:$port/api/v1/create-counter?name=$name",
                        null,
                        String::class.java
                    )
                )
                cdl.countDown()
            }
        }

        cdl.await()
        executors.shutdownNow()

        var failureCount = 0

        results.forEach { result ->
            assertNotNull(result)
            if (result.contains("already exists")) {
                failureCount++
            }
        }

        assertEquals(times - 1, failureCount)

        val end = testRestTemplate.getForObject(
            "http://localhost:$port/api/v1/get-counter?name=$name",
            CounterDto.Counter::class.java
        )

        assertEquals(0L, end.value)
    }
}
