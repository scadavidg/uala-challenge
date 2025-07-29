package com.data.local

import com.data.local.dao.FavoriteCityDao
import com.data.local.entity.FavoriteCityEntity
import com.domain.models.City
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FavoriteCityRoomDataSourceTest {

    private lateinit var dataSource: FavoriteCityRoomDataSource
    private lateinit var mockFavoriteCityDao: FavoriteCityDao

    private val testFavoriteCities = listOf(
        City(id = 1, name = "Alabama", country = "US", lat = 32.3182, lon = -86.9023, isFavorite = true),
        City(id = 2, name = "Albuquerque", country = "US", lat = 35.0844, lon = -106.6504, isFavorite = true)
    )

    private val testFavoriteEntities = listOf(
        FavoriteCityEntity(id = 1, name = "Alabama", country = "US", lat = 32.3182, lon = -86.9023),
        FavoriteCityEntity(id = 2, name = "Albuquerque", country = "US", lat = 35.0844, lon = -106.6504)
    )

    @BeforeEach
    fun setup() {
        mockFavoriteCityDao = mockk()
        dataSource = FavoriteCityRoomDataSource(mockFavoriteCityDao)
    }

    @Test
    fun `Given favorite cities exist in database, When getAllFavoriteCities is called, Then should return all favorite cities`() = runTest {
        // Given
        coEvery { mockFavoriteCityDao.getAllFavoriteCities() } returns testFavoriteEntities

        // When
        val result = dataSource.getAllFavoriteCities()

        // Then
        assertEquals(2, result.size)
        assertEquals("Alabama", result[0].name)
        assertEquals("Albuquerque", result[1].name)
        assertTrue(result.all { it.isFavorite })
    }

    @Test
    fun `Given search prefix is provided, When searchFavoriteCities is called, Then should return filtered favorite cities`() = runTest {
        // Given
        val searchPrefix = "Al"
        val filteredEntities = listOf(testFavoriteEntities[0]) // Only Alabama
        coEvery { mockFavoriteCityDao.searchFavoriteCities(searchPrefix) } returns filteredEntities

        // When
        val result = dataSource.searchFavoriteCities(searchPrefix)

        // Then
        assertEquals(1, result.size)
        assertEquals("Alabama", result[0].name)
        assertTrue(result[0].isFavorite)
    }

    @Test
    fun `Given favorite city exists in database, When getFavoriteCityById is called, Then should return favorite city`() = runTest {
        // Given
        val cityId = 1
        coEvery { mockFavoriteCityDao.getFavoriteCityById(cityId) } returns testFavoriteEntities[0]

        // When
        val result = dataSource.getFavoriteCityById(cityId)

        // Then
        assertNotNull(result)
        assertEquals(cityId, result?.id)
        assertEquals("Alabama", result?.name)
        assertTrue(result?.isFavorite == true)
    }

    @Test
    fun `Given favorite city does not exist in database, When getFavoriteCityById is called, Then should return null`() = runTest {
        // Given
        val cityId = 999
        coEvery { mockFavoriteCityDao.getFavoriteCityById(cityId) } returns null

        // When
        val result = dataSource.getFavoriteCityById(cityId)

        // Then
        assertEquals(null, result)
    }

    @Test
    fun `Given favorite city IDs are requested, When getFavoriteCityIds is called, Then should return list of favorite city IDs`() = runTest {
        // Given
        val expectedIds = listOf(1, 2)
        coEvery { mockFavoriteCityDao.getFavoriteCityIds() } returns expectedIds

        // When
        val result = dataSource.getFavoriteCityIds()

        // Then
        assertEquals(expectedIds, result)
    }

    @Test
    fun `Given city is favorite, When isFavorite is called, Then should return true`() = runTest {
        // Given
        val cityId = 1
        coEvery { mockFavoriteCityDao.isFavorite(cityId) } returns true

        // When
        val result = dataSource.isFavorite(cityId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `Given city is not favorite, When isFavorite is called, Then should return false`() = runTest {
        // Given
        val cityId = 999
        coEvery { mockFavoriteCityDao.isFavorite(cityId) } returns false

        // When
        val result = dataSource.isFavorite(cityId)

        // Then
        assertFalse(result)
    }

    @Test
    fun `Given favorite cities count is requested, When getFavoriteCitiesCount is called, Then should return correct count`() = runTest {
        // Given
        val expectedCount = 2
        coEvery { mockFavoriteCityDao.getFavoriteCitiesCount() } returns expectedCount

        // When
        val result = dataSource.getFavoriteCitiesCount()

        // Then
        assertEquals(expectedCount, result)
    }

    @Test
    fun `Given city is provided, When addFavoriteCity is called, Then should insert city into favorites`() = runTest {
        // Given
        val cityToAdd = testFavoriteCities[0]
        coEvery { mockFavoriteCityDao.insertFavoriteCity(any()) } returns Unit

        // When
        dataSource.addFavoriteCity(cityToAdd)

        // Then
        coVerify { mockFavoriteCityDao.insertFavoriteCity(any()) }
    }

    @Test
    fun `Given cities list is provided, When addFavoriteCities is called, Then should insert all cities into favorites`() = runTest {
        // Given
        coEvery { mockFavoriteCityDao.insertFavoriteCities(any()) } returns Unit

        // When
        dataSource.addFavoriteCities(testFavoriteCities)

        // Then
        coVerify { mockFavoriteCityDao.insertFavoriteCities(any()) }
    }

    @Test
    fun `Given city ID is provided, When removeFavoriteCity is called, Then should remove city from favorites`() = runTest {
        // Given
        val cityId = 1
        coEvery { mockFavoriteCityDao.removeFavoriteCity(cityId) } returns Unit

        // When
        dataSource.removeFavoriteCity(cityId)

        // Then
        coVerify { mockFavoriteCityDao.removeFavoriteCity(cityId) }
    }

    @Test
    fun `Given cleanup is requested, When deleteAllFavoriteCities is called, Then should delete all favorite cities`() = runTest {
        // Given
        coEvery { mockFavoriteCityDao.deleteAllFavoriteCities() } returns Unit

        // When
        dataSource.deleteAllFavoriteCities()

        // Then
        coVerify { mockFavoriteCityDao.deleteAllFavoriteCities() }
    }
}
