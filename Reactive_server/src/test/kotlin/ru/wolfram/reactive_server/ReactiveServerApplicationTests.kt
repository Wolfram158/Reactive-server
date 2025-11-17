package ru.wolfram.reactive_server

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.await
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import ru.wolfram.reactive_server.dto.CounterDto
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import kotlin.test.assertContains
import kotlin.test.assertEquals

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReactiveServerApplicationTests {
    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate

    @LocalServerPort
    private val port: Int = 0

    @Autowired
    private lateinit var r2dbcTemplate: DatabaseClient

    @BeforeEach
    fun prepare() = runTest {
        r2dbcTemplate.sql(
            """create table counter (
                id bigserial primary key,
                name varchar(64) not null unique,
                value bigint not null default 0
            );""".trimIndent()
        ).await()
        r2dbcTemplate.sql("create index if not exists idx_counter_name on counter(name);").await()
    }

    @AfterEach
    fun clean() = runTest {
        r2dbcTemplate.sql("delete from counter").await()
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
            registry.add("spring.flyway.enabled", { false })
            registry.add("spring.r2dbc.url") {
                "r2dbc:postgresql://${postgres.host}:${postgres.firstMappedPort}/${postgres.databaseName}"
            }
            registry.add("spring.r2dbc.username", postgres::getUsername)
            registry.add("spring.r2dbc.password", postgres::getPassword)
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
