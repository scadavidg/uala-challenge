package com.data.repositories

import com.data.dto.CityRemoteDto
import com.data.dto.CoordinatesDto
import com.data.local.AppSettingsDataSource
import com.data.local.CityRoomDataSource
import com.data.local.FavoriteCityRoomDataSource
import com.data.mapper.CityMapper
import com.data.remote.CityRemoteDataSource
import com.data.remote.NetworkException
import com.domain.models.City
import com.domain.models.Result
import io.mockk.coEvery
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CityRepositoryImplTest {

    private lateinit var repository: CityRepositoryImpl
    private lateinit var mockRemoteDataSource: CityRemoteDataSource
    private lateinit var mockRoomDataSource: CityRoomDataSource
    private lateinit var mockFavoriteCityDataSource: FavoriteCityRoomDataSource
    private lateinit var mockAppSettingsDataSource: AppSettingsDataSource
    private lateinit var mockMapper: CityMapper

    private val testCities = listOf(
        CityRemoteDto(_id = 1, name = "Alabama", country = "US", coordinates = CoordinatesDto(lon = -86.9023, lat = 32.3182)),
        CityRemoteDto(_id = 2, name = "Albuquerque", country = "US", coordinates = CoordinatesDto(lon = -106.6504, lat = 35.0844)),
        CityRemoteDto(_id = 3, name = "Anaheim", country = "US", coordinates = CoordinatesDto(lon = -117.9143, lat = 33.8366)),
        CityRemoteDto(_id = 4, name = "Arizona", country = "US", coordinates = CoordinatesDto(lon = -111.4312, lat = 33.7298)),
        CityRemoteDto(_id = 5, name = "Sydney", country = "AU", coordinates = CoordinatesDto(lon = 151.2093, lat = -33.8688))
    )

    @BeforeEach
    fun setup() {
        mockRemoteDataSource = mockk()
        mockRoomDataSource = mockk()
        mockFavoriteCityDataSource = mockk()
        mockAppSettingsDataSource = mockk()
        mockMapper = mockk()

        // Setup app settings to return offline mode by default
        coEvery { mockAppSettingsDataSource.isOnlineMode() } returns false

        // Setup mapper to return domain cities
        testCities.forEach { dto ->
            coEvery { mockMapper.mapToDomain(dto, any()) } returns City(
                id = dto._id,
                name = dto.name,
                country = dto.country,
                lat = dto.coordinates.lat,
                lon = dto.coordinates.lon,
                isFavorite = false
            )
        }

        // Setup room data source to return test cities
        coEvery { mockRoomDataSource.getAllCities() } returns testCities.map { dto ->
            City(
                id = dto._id,
                name = dto.name,
                country = dto.country,
                lat = dto.coordinates.lat,
                lon = dto.coordinates.lon,
                isFavorite = false
            )
        }
        // Setup room data source to return filtered results based on prefix
        coEvery { mockRoomDataSource.searchCitiesByPrefix("A") } returns listOf(
            City(id = 1, name = "Alabama", country = "US", lat = 32.3182, lon = -86.9023, isFavorite = false),
            City(id = 2, name = "Albuquerque", country = "US", lat = 35.0844, lon = -106.6504, isFavorite = false),
            City(id = 3, name = "Anaheim", country = "US", lat = 33.8366, lon = -117.9143, isFavorite = false),
            City(id = 4, name = "Arizona", country = "US", lat = 33.7298, lon = -111.4312, isFavorite = false)
        )
        coEvery { mockRoomDataSource.searchCitiesByPrefix("Alb") } returns listOf(
            City(id = 2, name = "Albuquerque", country = "US", lat = 35.0844, lon = -106.6504, isFavorite = false)
        )
        coEvery { mockRoomDataSource.searchCitiesByPrefix("s") } returns listOf(
            City(id = 5, name = "Sydney", country = "AU", lat = -33.8688, lon = 151.2093, isFavorite = false)
        )
        coEvery { mockRoomDataSource.searchCitiesByPrefix("") } returns testCities.map { dto ->
            City(
                id = dto._id,
                name = dto.name,
                country = dto.country,
                lat = dto.coordinates.lat,
                lon = dto.coordinates.lon,
                isFavorite = false
            )
        }
        coEvery { mockRoomDataSource.searchCitiesByPrefix("xyz") } returns emptyList()
        coEvery { mockRoomDataSource.searchCitiesByPrefix("  A  ") } returns listOf(
            City(id = 1, name = "Alabama", country = "US", lat = 32.3182, lon = -86.9023, isFavorite = false),
            City(id = 2, name = "Albuquerque", country = "US", lat = 35.0844, lon = -106.6504, isFavorite = false),
            City(id = 3, name = "Anaheim", country = "US", lat = 33.8366, lon = -117.9143, isFavorite = false),
            City(id = 4, name = "Arizona", country = "US", lat = 33.7298, lon = -111.4312, isFavorite = false)
        )
        // Setup case insensitive search - todas las variantes devuelven las mismas 4 ciudades
        val caseInsensitiveResults = listOf(
            City(id = 1, name = "Alabama", country = "US", lat = 32.3182, lon = -86.9023, isFavorite = false),
            City(id = 2, name = "Albuquerque", country = "US", lat = 35.0844, lon = -106.6504, isFavorite = false),
            City(id = 3, name = "Anaheim", country = "US", lat = 33.8366, lon = -117.9143, isFavorite = false),
            City(id = 4, name = "Arizona", country = "US", lat = 33.7298, lon = -111.4312, isFavorite = false)
        )
        coEvery { mockRoomDataSource.searchCitiesByPrefix("al") } returns caseInsensitiveResults
        coEvery { mockRoomDataSource.searchCitiesByPrefix("AL") } returns caseInsensitiveResults
        coEvery { mockRoomDataSource.searchCitiesByPrefix("Al") } returns caseInsensitiveResults

        // Setup favorite city data source
        coEvery { mockFavoriteCityDataSource.getFavoriteCityIds() } returns emptyList()
        coEvery { mockFavoriteCityDataSource.isFavorite(any()) } returns false

        repository = CityRepositoryImpl(mockRemoteDataSource, mockRoomDataSource, mockFavoriteCityDataSource, mockAppSettingsDataSource, mockMapper)
    }

    @Test
    fun `Given search prefix A, When searchCities is called, Then should return all cities starting with A`() = runTest {
        // Given
        val searchPrefix = "A"
        val onlyFavorites = false

        // When
        val result = repository.searchCities(searchPrefix, onlyFavorites)

        // Then
        assertTrue(result is Result.Success)
        val cities = (result as Result.Success).data
        assertEquals(4, cities.size)
        assertTrue(cities.all { it.name.startsWith("A", ignoreCase = true) })
        assertTrue(cities.any { it.name == "Alabama" })
        assertTrue(cities.any { it.name == "Albuquerque" })
        assertTrue(cities.any { it.name == "Anaheim" })
        assertTrue(cities.any { it.name == "Arizona" })
    }

    @Test
    fun `Given search prefix s, When searchCities is called, Then should return only Sydney`() = runTest {
        // Given
        val searchPrefix = "s"
        val onlyFavorites = false

        // When
        val result = repository.searchCities(searchPrefix, onlyFavorites)

        // Then
        assertTrue(result is Result.Success)
        val cities = (result as Result.Success).data
        assertEquals(1, cities.size)
        assertEquals("Sydney", cities.first().name)
    }

    @Test
    fun `Given search prefix Al, When searchCities is called, Then should return Alabama and Albuquerque`() = runTest {
        // Given
        val searchPrefix = "Al"
        val onlyFavorites = false

        // When
        val result = repository.searchCities(searchPrefix, onlyFavorites)

        // Then
        assertTrue(result is Result.Success)
        val cities = (result as Result.Success).data
        assertEquals(4, cities.size)
        assertTrue(cities.any { it.name == "Alabama" })
        assertTrue(cities.any { it.name == "Albuquerque" })
    }

    @Test
    fun `Given search prefix Alb, When searchCities is called, Then should return only Albuquerque`() = runTest {
        // Given
        val searchPrefix = "Alb"
        val onlyFavorites = false

        // When
        val result = repository.searchCities(searchPrefix, onlyFavorites)

        // Then
        assertTrue(result is Result.Success)
        val cities = (result as Result.Success).data
        assertEquals(1, cities.size)
        assertEquals("Albuquerque", cities.first().name)
    }

    @Test
    fun `Given empty search prefix, When searchCities is called, Then should return all cities`() = runTest {
        // Given
        val searchPrefix = ""
        val onlyFavorites = false

        // When
        val result = repository.searchCities(searchPrefix, onlyFavorites)

        // Then
        assertTrue(result is Result.Success)
        val cities = (result as Result.Success).data
        assertEquals(5, cities.size)
    }

    @Test
    fun `Given non-existent search prefix, When searchCities is called, Then should return empty list`() = runTest {
        // Given
        val searchPrefix = "xyz"
        val onlyFavorites = false

        // When
        val result = repository.searchCities(searchPrefix, onlyFavorites)

        // Then
        assertTrue(result is Result.Success)
        val cities = (result as Result.Success).data
        assertEquals(0, cities.size)
    }

    @Test
    fun `Given different case search prefixes, When searchCities is called, Then should be case insensitive`() = runTest {
        // Given
        val searchPrefix1 = "al"
        val searchPrefix2 = "AL"
        val searchPrefix3 = "Al"
        val onlyFavorites = false

        // When
        val result1 = repository.searchCities(searchPrefix1, onlyFavorites)
        val result2 = repository.searchCities(searchPrefix2, onlyFavorites)
        val result3 = repository.searchCities(searchPrefix3, onlyFavorites)

        // Then
        assertTrue(result1 is Result.Success)
        assertTrue(result2 is Result.Success)
        assertTrue(result3 is Result.Success)

        val cities1 = (result1 as Result.Success).data
        val cities2 = (result2 as Result.Success).data
        val cities3 = (result3 as Result.Success).data

        assertEquals(cities1.size, cities2.size)
        assertEquals(cities1.size, cities3.size)
    }

    @Test
    fun `Given search with only favorites filter, When searchCities is called, Then should filter by favorites`() = runTest {
        // Given
        val searchPrefix = "A"
        val onlyFavorites = true
        val favoriteIds = setOf(1, 3) // Alabama and Anaheim
        coEvery { mockFavoriteCityDataSource.getFavoriteCityIds() } returns favoriteIds.toList()

        // Setup searchFavoriteCities to return filtered favorites
        coEvery { mockFavoriteCityDataSource.searchFavoriteCities("A") } returns listOf(
            City(id = 1, name = "Alabama", country = "US", lat = 32.3182, lon = -86.9023, isFavorite = true),
            City(id = 3, name = "Anaheim", country = "US", lat = 33.8366, lon = -117.9143, isFavorite = true)
        )

        // When
        val result = repository.searchCities(searchPrefix, onlyFavorites)

        // Then
        assertTrue(result is Result.Success)
        val cities = (result as Result.Success).data
        assertEquals(2, cities.size)
        assertTrue(cities.all { it.isFavorite })
        assertTrue(cities.any { it.name == "Alabama" })
        assertTrue(cities.any { it.name == "Anaheim" })
    }

    @Test
    fun `Given search prefix with whitespace, When searchCities is called, Then should trim whitespace`() = runTest {
        // Given
        val searchPrefix = "  A  "
        val onlyFavorites = false

        // When
        val result = repository.searchCities(searchPrefix, onlyFavorites)

        // Then
        assertTrue(result is Result.Success)
        val cities = (result as Result.Success).data
        assertEquals(4, cities.size)
        assertTrue(cities.all { it.name.startsWith("A", ignoreCase = true) })
    }

    @Test
    fun `Given search prefix with special characters, When searchCities is called, Then should handle special characters`() = runTest {
        // Given
        val searchPrefix = "são"
        val onlyFavorites = false
        val specialCity = CityRemoteDto(_id = 6, name = "São Paulo", country = "BR", coordinates = CoordinatesDto(lon = -46.6333, lat = -23.5505))
        val allCities = testCities + specialCity

        coEvery { mockRoomDataSource.searchCitiesByPrefix("são") } returns listOf(
            City(
                id = specialCity._id,
                name = specialCity.name,
                country = specialCity.country,
                lat = specialCity.coordinates.lat,
                lon = specialCity.coordinates.lon,
                isFavorite = false
            )
        )

        // When
        val result = repository.searchCities(searchPrefix, onlyFavorites)

        // Then
        assertTrue(result is Result.Success)
        val cities = (result as Result.Success).data
        assertEquals(1, cities.size)
        assertEquals("São Paulo", cities.first().name)
    }

    @Test
    fun `Given long search query, When searchCities is called, Then should return error`() = runTest {
        // Given
        val longQuery = "a".repeat(CityRepositoryConstants.MAX_SEARCH_QUERY_LENGTH + 1)
        val onlyFavorites = false

        // When
        val result = repository.searchCities(longQuery, onlyFavorites)

        // Then
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).message.contains("Search query too long"))
    }

    @Test
    fun `Given online mode is enabled, When searchCities is called, Then should use remote data source`() = runTest {
        // Given
        val searchPrefix = "test"
        val onlyFavorites = false
        coEvery { mockAppSettingsDataSource.isOnlineMode() } returns true
        coEvery {
            mockRemoteDataSource.searchCities(
                prefix = searchPrefix,
                onlyFavorites = onlyFavorites,
                page = CityRepositoryConstants.DEFAULT_PAGE,
                limit = CityRepositoryConstants.DEFAULT_SEARCH_LIMIT
            )
        } returns mockk {
            coEvery { data } returns testCities
        }
        coEvery { mockFavoriteCityDataSource.getFavoriteCityIds() } returns emptyList()

        // When
        val result = repository.searchCities(searchPrefix, onlyFavorites)

        // Then
        assertTrue(result is Result.Success)
        val cities = (result as Result.Success).data
        assertEquals(5, cities.size)
    }

    @Test
    fun `Given online mode with network error, When searchCities is called, Then should return network error`() = runTest {
        // Given
        val searchPrefix = "test"
        val onlyFavorites = false
        coEvery { mockAppSettingsDataSource.isOnlineMode() } returns true
        coEvery {
            mockRemoteDataSource.searchCities(any(), any(), any(), any())
        } throws NetworkException("Connection failed")

        // When
        val result = repository.searchCities(searchPrefix, onlyFavorites)

        // Then
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).message.contains("Network error"))
    }

    @Test
    fun `Given online mode with IO exception, When searchCities is called, Then should return connection error`() = runTest {
        // Given
        val searchPrefix = "test"
        val onlyFavorites = false
        coEvery { mockAppSettingsDataSource.isOnlineMode() } returns true
        coEvery {
            mockRemoteDataSource.searchCities(any(), any(), any(), any())
        } throws IOException("Socket timeout")

        // When
        val result = repository.searchCities(searchPrefix, onlyFavorites)

        // Then
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).message.contains("Network error"))
    }

    @Test
    fun `Given offline mode, When getAllCities is called, Then should return local cities`() = runTest {
        // Given
        val page = 1
        val limit = 20

        // When
        val result = repository.getAllCities(page, limit)

        // Then
        assertTrue(result is Result.Success)
        val cities = (result as Result.Success).data
        assertEquals(5, cities.size)
    }

    @Test
    fun `Given online mode, When getAllCities is called, Then should return remote cities`() = runTest {
        // Given
        val page = 1
        val limit = 20
        coEvery { mockAppSettingsDataSource.isOnlineMode() } returns true
        coEvery { mockRemoteDataSource.downloadCities(page = page, limit = limit) } returns mockk {
            coEvery { data } returns testCities
        }

        // When
        val result = repository.getAllCities(page, limit)

        // Then
        assertTrue(result is Result.Success)
        val cities = (result as Result.Success).data
        assertEquals(5, cities.size)
    }

    @Test
    fun `Given city is not favorite, When toggleFavorite is called, Then should add to favorites`() = runTest {
        // Given
        val cityId = 1
        coEvery { mockFavoriteCityDataSource.isFavorite(cityId) } returns false
        coEvery { mockFavoriteCityDataSource.addFavoriteCity(any()) } returns Unit
        coEvery { mockFavoriteCityDataSource.removeFavoriteCity(cityId) } returns Unit
        coEvery { mockRoomDataSource.getCityById(cityId) } returns City(
            id = 1,
            name = "Alabama",
            country = "US",
            lat = 32.3182,
            lon = -86.9023,
            isFavorite = false
        )

        // When
        val result = repository.toggleFavorite(cityId)

        // Then
        assertTrue(result is Result.Success)
    }

    @Test
    fun `Given city is already favorite, When toggleFavorite is called, Then should remove from favorites`() = runTest {
        // Given
        val cityId = 1
        coEvery { mockFavoriteCityDataSource.isFavorite(cityId) } returns true
        coEvery { mockFavoriteCityDataSource.removeFavoriteCity(cityId) } returns Unit

        // When
        val result = repository.toggleFavorite(cityId)

        // Then
        assertTrue(result is Result.Success)
    }

    @Test
    fun `Given favorite cities exist, When getFavoriteCities is called, Then should return only favorite cities`() = runTest {
        // Given
        val favoriteCities = listOf(
            City(id = 1, name = "Alabama", country = "US", lat = 32.3182, lon = -86.9023, isFavorite = true),
            City(id = 3, name = "Anaheim", country = "US", lat = 33.8366, lon = -117.9143, isFavorite = true)
        )
        coEvery { mockFavoriteCityDataSource.getAllFavoriteCities() } returns favoriteCities

        // When
        val result = repository.getFavoriteCities()

        // Then
        assertTrue(result is Result.Success)
        val cities = (result as Result.Success).data
        assertEquals(2, cities.size)
        assertTrue(cities.all { it.isFavorite })
    }

    @Test
    fun `Given offline mode, When getCityById is called, Then should return local city`() = runTest {
        // Given
        val cityId = 1
        coEvery { mockRoomDataSource.getCityById(cityId) } returns City(
            id = 1,
            name = "Alabama",
            country = "US",
            lat = 32.3182,
            lon = -86.9023,
            isFavorite = false
        )
        coEvery { mockFavoriteCityDataSource.isFavorite(cityId) } returns false

        // When
        val result = repository.getCityById(cityId)

        // Then
        assertTrue(result is Result.Success)
        val city = (result as Result.Success).data
        assertEquals(cityId, city?.id)
        assertEquals("Alabama", city?.name)
    }

    @Test
    fun `Given online mode, When getCityById is called, Then should return remote city`() = runTest {
        // Given
        val cityId = 1
        coEvery { mockAppSettingsDataSource.isOnlineMode() } returns true
        coEvery { mockRemoteDataSource.getCityById(cityId) } returns testCities.first()
        coEvery { mockFavoriteCityDataSource.isFavorite(cityId) } returns false

        // When
        val result = repository.getCityById(cityId)

        // Then
        assertTrue(result is Result.Success)
        val city = (result as Result.Success).data
        assertEquals(cityId, city?.id)
        assertEquals("Alabama", city?.name)
    }

    @Test
    fun `Given online mode is enabled, When toggleOnlineMode is called, Then should update online mode`() = runTest {
        // Given
        val enabled = true
        coEvery { mockAppSettingsDataSource.setOnlineMode(enabled) } returns Unit

        // When
        val result = repository.toggleOnlineMode(enabled)

        // Then
        assertTrue(result is Result.Success)
    }

    @Test
    fun `Given online mode is enabled, When isOnlineMode is called, Then should return current mode`() = runTest {
        // Given
        coEvery { mockAppSettingsDataSource.isOnlineMode() } returns true

        // When
        val result = repository.isOnlineMode()

        // Then
        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).data)
    }

    @Test
    fun `Given unsorted cities, When searchCities is called, Then should sort results alphabetically`() = runTest {
        // Given
        val searchPrefix = ""
        val onlyFavorites = false
        val unsortedCities = listOf(
            CityRemoteDto(_id = 1, name = "Zebra", country = "US", coordinates = CoordinatesDto(lon = 0.0, lat = 0.0)),
            CityRemoteDto(_id = 2, name = "Alpha", country = "US", coordinates = CoordinatesDto(lon = 0.0, lat = 0.0)),
            CityRemoteDto(_id = 3, name = "Beta", country = "US", coordinates = CoordinatesDto(lon = 0.0, lat = 0.0))
        )

        coEvery { mockRoomDataSource.searchCitiesByPrefix("") } returns unsortedCities.map { dto ->
            City(
                id = dto._id,
                name = dto.name,
                country = dto.country,
                lat = dto.coordinates.lat,
                lon = dto.coordinates.lon,
                isFavorite = false
            )
        }
        coEvery { mockMapper.mapToDomain(any(), any()) } answers {
            val dto = firstArg<CityRemoteDto>()
            City(
                id = dto._id,
                name = dto.name,
                country = dto.country,
                lat = dto.coordinates.lat,
                lon = dto.coordinates.lon,
                isFavorite = false
            )
        }

        // When
        val result = repository.searchCities(searchPrefix, onlyFavorites)

        // Then
        assertTrue(result is Result.Success)
        val cities = (result as Result.Success).data
        assertEquals("Alpha", cities[0].name)
        assertEquals("Beta", cities[1].name)
        assertEquals("Zebra", cities[2].name)
    }
}
