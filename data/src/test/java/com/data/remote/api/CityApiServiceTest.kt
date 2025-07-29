package com.data.remote.api

import com.data.dto.ApiResponseDto
import com.data.dto.CityRemoteDto
import com.data.dto.CoordinatesDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class CityApiServiceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var cityApiService: CityApiService

    private val testCities = listOf(
        CityRemoteDto(_id = 1, name = "Alabama", country = "US", coordinates = CoordinatesDto(lon = -86.9023, lat = 32.3182)),
        CityRemoteDto(_id = 2, name = "Albuquerque", country = "US", coordinates = CoordinatesDto(lon = -106.6504, lat = 35.0844)),
        CityRemoteDto(_id = 3, name = "Anaheim", country = "US", coordinates = CoordinatesDto(lon = -117.9143, lat = 33.8366))
    )

    @BeforeEach
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        cityApiService = retrofit.create(CityApiService::class.java)
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `getCities default parameters`() = runTest {
        // Given
        val response = ApiResponseDto(success = true, data = testCities)
        val responseJson = """
            {
                "success": true,
                "data": [
                    {"_id": 1, "name": "Alabama", "country": "US", "coord": {"lon": -86.9023, "lat": 32.3182}},
                    {"_id": 2, "name": "Albuquerque", "country": "US", "coord": {"lon": -106.6504, "lat": 35.0844}},
                    {"_id": 3, "name": "Anaheim", "country": "US", "coord": {"lon": -117.9143, "lat": 33.8366}}
                ]
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseJson))

        // When
        val result = cityApiService.getCities()

        // Then
        assertNotNull(result)
        assertTrue(result.success)
        assertEquals(3, result.data.size)
        assertEquals("Alabama", result.data[0].name)
        assertEquals("Albuquerque", result.data[1].name)
        assertEquals("Anaheim", result.data[2].name)
    }

    @Test
    fun `getCities specific page and limit`() = runTest {
        // Given
        val responseJson = """
            {
                "success": true,
                "data": [
                    {"_id": 1, "name": "Alabama", "country": "US", "coord": {"lon": -86.9023, "lat": 32.3182}}
                ]
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseJson))

        // When
        val result = cityApiService.getCities(page = 2, limit = 1)

        // Then
        assertNotNull(result)
        assertTrue(result.success)
        assertEquals(1, result.data.size)
        assertEquals("Alabama", result.data[0].name)
    }

    @Test
    fun `getCities empty result`() = runTest {
        // Given
        val responseJson = """
            {
                "success": true,
                "data": []
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseJson))

        // When
        val result = cityApiService.getCities(page = 999, limit = 20)

        // Then
        assertNotNull(result)
        assertTrue(result.success)
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun `getCities invalid page negative`() = runTest {
        // Given
        val responseJson = """
            {
                "success": false,
                "data": []
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseJson))

        // When
        val result = cityApiService.getCities(page = -1, limit = 20)

        // Then
        assertNotNull(result)
        // API might handle negative page gracefully or return error
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun `getCities invalid limit negative`() = runTest {
        // Given
        val responseJson = """
            {
                "success": false,
                "data": []
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseJson))

        // When
        val result = cityApiService.getCities(page = 1, limit = -5)

        // Then
        assertNotNull(result)
        // API might handle negative limit gracefully or return error
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun `getCities zero limit`() = runTest {
        // Given
        val responseJson = """
            {
                "success": true,
                "data": []
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseJson))

        // When
        val result = cityApiService.getCities(page = 1, limit = 0)

        // Then
        assertNotNull(result)
        assertTrue(result.success)
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun `getCities max limit`() = runTest {
        // Given
        val responseJson = """
            {
                "success": true,
                "data": [
                    {"_id": 1, "name": "Alabama", "country": "US", "coord": {"lon": -86.9023, "lat": 32.3182}}
                ]
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseJson))

        // When
        val result = cityApiService.getCities(page = 1, limit = 1000)

        // Then
        assertNotNull(result)
        assertTrue(result.success)
        assertEquals(1, result.data.size)
    }

    @Test
    fun `getCities network error`() = runTest {
        // Given
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Network Error"))

        // When & Then
        assertThrows<Exception> {
            cityApiService.getCities()
        }
    }

    @Test
    fun `getCities server error 5xx`() = runTest {
        // Given
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Internal Server Error"))

        // When & Then
        assertThrows<Exception> {
            cityApiService.getCities()
        }
    }

    @Test
    fun `getCities client error 4xx`() = runTest {
        // Given
        mockWebServer.enqueue(MockResponse().setResponseCode(400).setBody("Bad Request"))

        // When & Then
        assertThrows<Exception> {
            cityApiService.getCities()
        }
    }

    @Test
    fun `getCities deserialization error`() = runTest {
        // Given
        val malformedJson = """{"success": true, "data": [{"invalid": "json"}]}"""
        mockWebServer.enqueue(MockResponse().setBody(malformedJson))

        // When & Then
        assertThrows<Exception> {
            cityApiService.getCities()
        }
    }

    @Test
    fun `searchCities with prefix only`() = runTest {
        // Given
        val responseJson = """
            {
                "success": true,
                "data": [
                    {"_id": 1, "name": "Alabama", "country": "US", "coord": {"lon": -86.9023, "lat": 32.3182}},
                    {"_id": 2, "name": "Albuquerque", "country": "US", "coord": {"lon": -106.6504, "lat": 35.0844}}
                ]
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseJson))

        // When
        val result = cityApiService.searchCities(prefix = "Al")

        // Then
        assertNotNull(result)
        assertTrue(result.success)
        assertEquals(2, result.data.size)
        assertTrue(result.data.all { it.name.startsWith("Al") })
    }

    @Test
    fun `searchCities with prefix and onlyFavorites true`() = runTest {
        // Given
        val responseJson = """
            {
                "success": true,
                "data": [
                    {"_id": 1, "name": "Alabama", "country": "US", "coord": {"lon": -86.9023, "lat": 32.3182}}
                ]
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseJson))

        // When
        val result = cityApiService.searchCities(prefix = "Al", onlyFavorites = true)

        // Then
        assertNotNull(result)
        assertTrue(result.success)
        assertEquals(1, result.data.size)
        assertEquals("Alabama", result.data[0].name)
    }

    @Test
    fun `searchCities with prefix onlyFavorites and pagination`() = runTest {
        // Given
        val responseJson = """
            {
                "success": true,
                "data": [
                    {"_id": 1, "name": "Alabama", "country": "US", "coord": {"lon": -86.9023, "lat": 32.3182}}
                ]
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseJson))

        // When
        val result = cityApiService.searchCities(prefix = "Al", onlyFavorites = true, page = 2, limit = 1)

        // Then
        assertNotNull(result)
        assertTrue(result.success)
        assertEquals(1, result.data.size)
        assertEquals("Alabama", result.data[0].name)
    }

    @Test
    fun `searchCities with null prefix default behavior`() = runTest {
        // Given
        val responseJson = """
            {
                "success": true,
                "data": [
                    {"_id": 1, "name": "Alabama", "country": "US", "coord": {"lon": -86.9023, "lat": 32.3182}},
                    {"_id": 2, "name": "Albuquerque", "country": "US", "coord": {"lon": -106.6504, "lat": 35.0844}},
                    {"_id": 3, "name": "Anaheim", "country": "US", "coord": {"lon": -117.9143, "lat": 33.8366}}
                ]
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseJson))

        // When
        val result = cityApiService.searchCities(prefix = null)

        // Then
        assertNotNull(result)
        assertTrue(result.success)
        assertEquals(3, result.data.size)
    }

    @Test
    fun `searchCities with empty string prefix`() = runTest {
        // Given
        val responseJson = """
            {
                "success": true,
                "data": [
                    {"_id": 1, "name": "Alabama", "country": "US", "coord": {"lon": -86.9023, "lat": 32.3182}},
                    {"_id": 2, "name": "Albuquerque", "country": "US", "coord": {"lon": -106.6504, "lat": 35.0844}},
                    {"_id": 3, "name": "Anaheim", "country": "US", "coord": {"lon": -117.9143, "lat": 33.8366}}
                ]
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseJson))

        // When
        val result = cityApiService.searchCities(prefix = "")

        // Then
        assertNotNull(result)
        assertTrue(result.success)
        assertEquals(3, result.data.size)
    }

    @Test
    fun `searchCities prefix no match`() = runTest {
        // Given
        val responseJson = """
            {
                "success": true,
                "data": []
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseJson))

        // When
        val result = cityApiService.searchCities(prefix = "XYZ")

        // Then
        assertNotNull(result)
        assertTrue(result.success)
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun `searchCities onlyFavorites true no match`() = runTest {
        // Given
        val responseJson = """
            {
                "success": true,
                "data": []
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseJson))

        // When
        val result = cityApiService.searchCities(prefix = "Al", onlyFavorites = true)

        // Then
        assertNotNull(result)
        assertTrue(result.success)
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun `searchCities invalid page for search`() = runTest {
        // Given
        val responseJson = """
            {
                "success": false,
                "data": []
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseJson))

        // When
        val result = cityApiService.searchCities(prefix = "Al", page = -1)

        // Then
        assertNotNull(result)
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun `searchCities invalid limit for search`() = runTest {
        // Given
        val responseJson = """
            {
                "success": false,
                "data": []
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseJson))

        // When
        val result = cityApiService.searchCities(prefix = "Al", limit = -5)

        // Then
        assertNotNull(result)
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun `searchCities special characters in prefix`() = runTest {
        // Given
        val responseJson = """
            {
                "success": true,
                "data": [
                    {"_id": 1, "name": "São Paulo", "country": "BR", "coord": {"lon": -46.6333, "lat": -23.5505}}
                ]
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseJson))

        // When
        val result = cityApiService.searchCities(prefix = "São")

        // Then
        assertNotNull(result)
        assertTrue(result.success)
        assertEquals(1, result.data.size)
        assertEquals("São Paulo", result.data[0].name)
    }

    @Test
    fun `searchCities case sensitivity`() = runTest {
        // Given
        val responseJson = """
            {
                "success": true,
                "data": [
                    {"_id": 1, "name": "Alabama", "country": "US", "coord": {"lon": -86.9023, "lat": 32.3182}}
                ]
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseJson))

        // When
        val result = cityApiService.searchCities(prefix = "al")

        // Then
        assertNotNull(result)
        assertTrue(result.success)
        assertEquals(1, result.data.size)
        assertEquals("Alabama", result.data[0].name)
    }

    @Test
    fun `searchCities network error`() = runTest {
        // Given
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Network Error"))

        // When & Then
        assertThrows<Exception> {
            cityApiService.searchCities(prefix = "Al")
        }
    }

    @Test
    fun `searchCities server error 5xx`() = runTest {
        // Given
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Internal Server Error"))

        // When & Then
        assertThrows<Exception> {
            cityApiService.searchCities(prefix = "Al")
        }
    }

    @Test
    fun `searchCities client error 4xx`() = runTest {
        // Given
        mockWebServer.enqueue(MockResponse().setResponseCode(400).setBody("Bad Request"))

        // When & Then
        assertThrows<Exception> {
            cityApiService.searchCities(prefix = "Al")
        }
    }

    @Test
    fun `searchCities deserialization error`() = runTest {
        // Given
        val malformedJson = """{"success": true, "data": [{"invalid": "json"}]}"""
        mockWebServer.enqueue(MockResponse().setBody(malformedJson))

        // When & Then
        assertThrows<Exception> {
            cityApiService.searchCities(prefix = "Al")
        }
    }

    @Test
    fun `getCityById valid ID`() = runTest {
        // Given
        val responseJson = """
            {
                "success": true,
                "data": {"_id": 1, "name": "Alabama", "country": "US", "coord": {"lon": -86.9023, "lat": 32.3182}}
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseJson))

        // When
        val result = cityApiService.getCityById(1)

        // Then
        assertNotNull(result)
        assertTrue(result.success)
        assertEquals(1, result.data._id)
        assertEquals("Alabama", result.data.name)
        assertEquals("US", result.data.country)
    }

    @Test
    fun `getCityById non existent ID`() = runTest {
        // Given
        mockWebServer.enqueue(MockResponse().setResponseCode(404).setBody("Not Found"))

        // When & Then
        assertThrows<Exception> {
            cityApiService.getCityById(99999)
        }
    }

    @Test
    fun `getCityById invalid ID format e g negative zero`() = runTest {
        // Given
        mockWebServer.enqueue(MockResponse().setResponseCode(400).setBody("Bad Request"))

        // When & Then
        assertThrows<Exception> {
            cityApiService.getCityById(-1)
        }
    }

    @Test
    fun `getCityById network error`() = runTest {
        // Given
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Network Error"))

        // When & Then
        assertThrows<Exception> {
            cityApiService.getCityById(1)
        }
    }

    @Test
    fun `getCityById server error 5xx`() = runTest {
        // Given
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Internal Server Error"))

        // When & Then
        assertThrows<Exception> {
            cityApiService.getCityById(1)
        }
    }

    @Test
    fun `getCityById client error 4xx e g 401 403 404`() = runTest {
        // Given
        mockWebServer.enqueue(MockResponse().setResponseCode(404).setBody("Not Found"))

        // When & Then
        assertThrows<Exception> {
            cityApiService.getCityById(999)
        }
    }

    @Test
    fun `getCityById deserialization error`() = runTest {
        // Given
        val malformedJson = """{"success": true, "data": {"invalid": "json"}}"""
        mockWebServer.enqueue(MockResponse().setBody(malformedJson))

        // When & Then
        assertThrows<Exception> {
            cityApiService.getCityById(1)
        }
    }

    @Test
    fun `getCityById integer max value for ID`() = runTest {
        // Given
        val responseJson = """
            {
                "success": true,
                "data": {"_id": 2147483647, "name": "Max City", "country": "US", "coord": {"lon": 0.0, "lat": 0.0}}
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseJson))

        // When
        val result = cityApiService.getCityById(Int.MAX_VALUE)

        // Then
        assertNotNull(result)
        assertTrue(result.success)
        assertEquals(Int.MAX_VALUE, result.data._id)
        assertEquals("Max City", result.data.name)
    }
}
