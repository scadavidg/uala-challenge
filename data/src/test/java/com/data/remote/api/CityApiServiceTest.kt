package com.data.remote.api

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.fail

@OptIn(ExperimentalCoroutinesApi::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CityApiServiceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var service: CityApiService

    @BeforeEach
    fun setup() {
        mockWebServer = MockWebServer()
        service = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(CityApiService::class.java)
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `Successful API call with valid city data`() = runTest {
        // Given
        val json = """[{"_id":1,"name":"CityA","country":"US","coord":{"lon":10.0,"lat":20.0}}]"""
        mockWebServer.enqueue(MockResponse().setBody(json).setResponseCode(200))

        // When
        val result = service.getCities()

        // Then
        assertEquals(1, result.size)
        assertEquals("CityA", result[0].name)
    }

    @Test
    fun `API call returns empty list`() = runTest {
        // Given
        mockWebServer.enqueue(MockResponse().setBody("[]").setResponseCode(200))

        // When
        val result = service.getCities()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `API call returns malformed JSON`() = runTest {
        // Given
        val malformedJson = """{"name": "CityA",,,}"""
        mockWebServer.enqueue(MockResponse().setBody(malformedJson).setResponseCode(200))

        // When - Then
        assertFailsWith<Exception> {
            service.getCities()
        }
    }

    @Test
    fun `API call returns unexpected JSON structure`() = runTest {
        // Given
        val unexpectedJson = """[{"unexpected":123}]"""
        mockWebServer.enqueue(MockResponse().setBody(unexpectedJson).setResponseCode(200))

        // When - Then
        assertFailsWith<Exception> {
            service.getCities()
        }
    }

    @Test
    fun `Network error during API call`() = runTest {
        // Given
        mockWebServer.shutdown()

        // When - Then
        assertFailsWith<IOException> {
            service.getCities()
        }
    }

    @Test
    fun `Server error 5xx during API call`() = runTest {
        // Given
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        // When - Then
        assertFailsWith<Exception> {
            service.getCities()
        }
    }

    @Test
    fun `Client error 4xx during API call`() = runTest {
        // Given
        mockWebServer.enqueue(MockResponse().setResponseCode(404))

        // When - Then
        assertFailsWith<Exception> {
            service.getCities()
        }
    }

    @Test
    fun `API call timeout`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setBody("[]")
                .setBodyDelay(10, TimeUnit.SECONDS)
        )

        // When - Then
        assertFailsWith<IOException> {
            service.getCities()
        }
    }

    @Test
    fun `Cancellation of the coroutine`() = runTest {
        // Given
        mockWebServer.enqueue(MockResponse().setBody("[]").setResponseCode(200).setBodyDelay(2, TimeUnit.SECONDS))

        val job = launch {
            service.getCities()
        }

        // When
        delay(500)
        job.cancelAndJoin()

        // Then
        assertTrue(job.isCancelled)
    }

    @Test
    fun `Successful API call with very large list of cities`() = runTest {
        // Given
        val cities = (1..1000).joinToString(separator = ",") {
            """{"_id":$it,"name":"City$it","country":"US","coord":{"lon":0.0,"lat":0.0}}"""
        }
        val json = "[$cities]"
        mockWebServer.enqueue(MockResponse().setBody(json).setResponseCode(200))

        // When
        val result = service.getCities()

        // Then
        assertEquals(1000, result.size)
    }

    @Test
    fun `API returns JSON with null values for non nullable fields in DTO`() = runTest {
        // Given
        val json = """[{"_id":null,"name":null,"country":null,"coord":{"lon":null,"lat":null}}]"""
        mockWebServer.enqueue(MockResponse().setBody(json).setResponseCode(200))

        // When - Then
        assertFailsWith<Exception> {
            service.getCities()
        }
    }

    @Test
    fun `API returns JSON with extra fields not in DTO`() = runTest {
        // Given
        val json = """[{"_id":1,"name":"CityA","country":"US","coord":{"lon":10.0,"lat":20.0},"extra":"ignored"}]"""
        mockWebServer.enqueue(MockResponse().setBody(json).setResponseCode(200))

        // When
        val result = service.getCities()

        // Then
        assertEquals("CityA", result.first().name)
    }

    @Test
    fun `API returns JSON with missing nullable fields in DTO`() = runTest {
        // Given
        val json = """[{"_id":1,"name":"CityA","country":"US"}]"""
        mockWebServer.enqueue(MockResponse().setBody(json).setResponseCode(200))

        // When - Then
        try {
            service.getCities()
            fail("Expected exception due to missing coord field")
        } catch (e: Exception) {
            Assertions.assertTrue(e is Exception)
        }
    }

    @Test
    fun `API returns JSON with incorrect data types for fields in DTO`() = runTest {
        // Given
        val json = """[{"_id":"wrong","name":true,"country":[],"coord":{"lon":"abc","lat":"xyz"}}]"""
        mockWebServer.enqueue(MockResponse().setBody(json).setResponseCode(200))

        // When - Then
        assertFailsWith<Exception> {
            service.getCities()
        }
    }

    @Test
    fun `Verify HTTP GET method and URL`() = runTest {
        // Given
        val json = "[]"
        mockWebServer.enqueue(MockResponse().setBody(json).setResponseCode(200))

        // When
        service.getCities()

        // Then
        val request = mockWebServer.takeRequest()
        Assertions.assertEquals("GET", request.method)
        Assertions.assertTrue(request.path!!.contains("cities.json"))
    }

    @Test
    fun `Concurrent API calls`() = runTest {
        // Given
        val json = """[{"_id":1,"name":"CityA","country":"US","coord":{"lon":10.0,"lat":20.0}}]"""
        repeat(10) {
            mockWebServer.enqueue(MockResponse().setBody(json).setResponseCode(200))
        }

        // When
        val results = (1..10).map {
            launch { service.getCities() }
        }
        results.forEach { it.join() }

        // Then
        Assertions.assertEquals(10, mockWebServer.requestCount)
    }
}
