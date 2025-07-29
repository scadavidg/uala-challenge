package com.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.data.dto.CityRemoteDto
import io.mockk.every
import io.mockk.mockk
import java.io.ByteArrayInputStream
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@OptIn(ExperimentalCoroutinesApi::class)
class CityLocalDataSourceImplTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var context: Context
    private lateinit var dataSource: CityLocalDataSource
    private lateinit var scope: CoroutineScope
    private val FAVORITES_KEY = CityLocalDataSourceImpl.FAVORITES_KEY

    @BeforeEach
    fun setup() {
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        // We use a temporary random filename per test
        val file = File(tempDir, "datastore_${System.nanoTime()}.preferences_pb")

        // We create the unique datastore instance per test
        dataStore = PreferenceDataStoreFactory.create(scope = scope) { file }

        // Mock the context with relaxed mode
        context = mockk(relaxed = true)

        // We inject the datastore and context into your class under test
        dataSource = CityLocalDataSourceImpl(dataStore, context)
    }

    @AfterEach
    fun tearDown() = runBlocking {
        // Delete the temp files
        delay(100)
        scope.cancel()
    }

    @Test
    fun `getFavoriteIds Empty DataStore`() = runTest {
        // Given an empty DataStore

        // When getFavoriteIds is called
        val result = dataSource.getFavoriteIds()

        // Then the result is an empty set
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getFavoriteIds Non empty DataStore with valid IDs`() = runTest {
        // Given a DataStore with valid favorite IDs
        dataStore.edit {
            it[FAVORITES_KEY] = setOf("1", "2", "3")
        }

        // When getFavoriteIds is called
        val result = dataSource.getFavoriteIds()

        // Then the result is a set of the corresponding integer IDs
        assertEquals(setOf(1, 2, 3), result)
    }

    @Test
    fun `getFavoriteIds DataStore with mixed valid and invalid IDs`() = runTest {
        // Given a DataStore with a mix of valid and invalid favorite IDs
        dataStore.edit {
            it[FAVORITES_KEY] = setOf("1", "a", "2", "b")
        }

        // When getFavoriteIds is called
        val result = dataSource.getFavoriteIds()

        // Then the result is a set of only the valid integer IDs
        assertEquals(setOf(1, 2), result)
    }

    @Test
    fun `getFavoriteIds DataStore with only invalid IDs`() = runTest {
        // Given a DataStore with only invalid favorite IDs
        dataStore.edit {
            it[FAVORITES_KEY] = setOf("abc", "xyz")
        }

        // When getFavoriteIds is called
        val result = dataSource.getFavoriteIds()

        // Then the result is an empty set
        assertTrue(result.isEmpty())
    }

    @Test
    fun `isFavorite ID exists in favorites`() = runTest {
        // Given a DataStore where the ID exists in favorites
        dataStore.edit {
            it[FAVORITES_KEY] = setOf("42")
        }

        // When isFavorite is called with that ID
        val result = dataSource.isFavorite(42)

        // Then the result is true
        assertTrue(result)
    }

    @Test
    fun `isFavorite ID does not exist in favorites`() = runTest {
        // Given a DataStore where the ID does not exist in favorites
        dataStore.edit {
            it[FAVORITES_KEY] = setOf("1", "2")
        }

        // When isFavorite is called with an ID not in the set
        val result = dataSource.isFavorite(3)

        // Then the result is false
        assertFalse(result)
    }

    @Test
    fun `isFavorite Empty favorites list`() = runTest {
        // Given an empty favorites list in DataStore

        // When isFavorite is called with any ID
        val result = dataSource.isFavorite(10)

        // Then the result is false
        assertFalse(result)
    }

    @Test
    fun `addFavorite Add a new ID to an empty list`() = runTest {
        // Given an empty favorites list

        // When a new ID is added
        dataSource.addFavorite(10)

        // Then the DataStore contains the new ID
        val result = dataStore.data.first()[FAVORITES_KEY]
        assertTrue(result?.contains("10") == true)
    }

    @Test
    fun `addFavorite Add an existing ID`() = runTest {
        // Given a favorites list containing an ID
        dataStore.edit { it[FAVORITES_KEY] = setOf("5") }

        // When the same ID is added again
        dataSource.addFavorite(5)

        // Then the DataStore remains unchanged (no duplicates)
        val result = dataStore.data.first()[FAVORITES_KEY]
        assertEquals(setOf("5"), result)
    }

    @Test
    fun `removeFavorite Attempt to remove a non existent ID`() = runTest {
        // Given a favorites list
        dataStore.edit { it[FAVORITES_KEY] = setOf("1", "2") }

        // When a non-existent ID is attempted to be removed
        dataSource.removeFavorite(5)

        // Then the DataStore remains unchanged
        val result = dataStore.data.first()[FAVORITES_KEY]
        assertEquals(setOf("1", "2"), result)
    }

    @Test
    fun `removeFavorite Attempt to remove from an empty list`() = runTest {
        // Given an empty favorites list

        // When an ID is attempted to be removed
        dataSource.removeFavorite(10)

        // Then the DataStore remains empty or the key is not present
        val result = dataStore.data.first()[FAVORITES_KEY]
        assertTrue(result?.isEmpty() ?: true)
    }

    @Test
    fun `Given json_sorted_min file exists, When getLocalCities is called, Then should load cities from file`() = runTest {
        // Given
        val jsonString = """[{"_id":1,"name":"Test","country":"Test","coord":{"lon":1.0,"lat":1.0}}]"""
        val inputStream = ByteArrayInputStream(jsonString.toByteArray())
        val mockAssets = mockk<android.content.res.AssetManager>(relaxed = true)
        every { context.assets } returns mockAssets
        every { mockAssets.open("json_sorted_min.json") } returns inputStream

        // Re-initialize dataSource with the mocked context
        dataSource = CityLocalDataSourceImpl(dataStore, context)

        // When
        val result = dataSource.getLocalCities()

        // Then
        assertEquals(1, result.size)
    }

    @Test
    fun `test mock setup`() = runTest {
        // Given
        val jsonString = """[{"_id":1,"name":"Test","country":"Test","coord":{"lon":1.0,"lat":1.0}}]"""
        val inputStream = ByteArrayInputStream(jsonString.toByteArray())
        val mockAssets = mockk<android.content.res.AssetManager>(relaxed = true)
        every { context.assets } returns mockAssets
        every { mockAssets.open("json_sorted_min.json") } returns inputStream

        // When
        val testStream = context.assets.open("json_sorted_min.json")
        val testContent = testStream.bufferedReader().use { it.readText() }

        // Then
        assertEquals(jsonString, testContent)
    }

    @Test
    fun `test JSON parsing`() = runTest {
        // Given
        val jsonString = """[{"_id":1,"name":"Test","country":"Test","coord":{"lon":1.0,"lat":1.0}}]"""
        val inputStream = ByteArrayInputStream(jsonString.toByteArray())
        val mockAssets = mockk<android.content.res.AssetManager>(relaxed = true)
        every { context.assets } returns mockAssets
        every { mockAssets.open("json_sorted_min.json") } returns inputStream

        // Re-initialize dataSource with the mocked context
        dataSource = CityLocalDataSourceImpl(dataStore, context)

        // When
        val result = dataSource.getLocalCities()

        // Then
        assertEquals(1, result.size)
        assertEquals(1, result[0]._id)
        assertEquals("Test", result[0].name)
        assertEquals("Test", result[0].country)
        assertEquals(1.0, result[0].coordinates.lon)
        assertEquals(1.0, result[0].coordinates.lat)
    }

    @Test
    fun `test direct JSON parsing`() = runTest {
        // Given
        val jsonString = """[{"_id":1,"name":"Test","country":"Test","coord":{"lon":1.0,"lat":1.0}}]"""

        // When - Parse JSON directly
        val moshi = com.squareup.moshi.Moshi.Builder()
            .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()
        val type = com.squareup.moshi.Types.newParameterizedType(List::class.java, com.data.dto.CityRemoteDto::class.java)
        val adapter = moshi.adapter<List<com.data.dto.CityRemoteDto>>(type)
        val result = adapter.fromJson(jsonString)

        // Then
        assertNotNull(result)
        assertEquals(1, result!!.size)
        assertEquals(1, result[0]._id)
        assertEquals("Test", result[0].name)
        assertEquals("Test", result[0].country)
        assertEquals(1.0, result[0].coordinates.lon)
        assertEquals(1.0, result[0].coordinates.lat)
    }

    @Test
    fun `getLocalCities returns empty list when file cannot be read`() = runTest {
        // Given
        val mockAssets = mockk<android.content.res.AssetManager>()
        every { context.assets } returns mockAssets
        every { mockAssets.open("json_sorted_min.json") } throws Exception("File not found")

        // When
        val result = dataSource.getLocalCities()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getLocalCities returns empty list for malformed JSON`() = runTest {
        // Given
        val malformedJson = "[{\"name\": \"Test\",,,}]"
        val inputStream = ByteArrayInputStream(malformedJson.toByteArray())
        val mockAssets = mockk<android.content.res.AssetManager>()
        every { context.assets } returns mockAssets
        every { mockAssets.open("json_sorted_min_min.json") } returns inputStream

        // When
        val result = dataSource.getLocalCities()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `Given json_sorted_min file contains multiple cities, When getLocalCities is called, Then should load first few cities from file`() =
        runTest {
            // Given
            val jsonString = """
            [
                {"_id":1,"name":"'t Zand","country":"NL","coord":{"lon":4.7556,"lat":52.8367}},
                {"_id":2,"name":"'t Hoeksken","country":"BE","coord":{"lon":3.9667,"lat":50.8000}},
                {"_id":3,"name":"665 Site Colonia","country":"US","coord":{"lon":-98.0333,"lat":27.7303}},
                {"_id":4,"name":"A Coruna","country":"ES","coord":{"lon":-8.3961,"lat":43.3713}},
                {"_id":5,"name":"Aachen","country":"DE","coord":{"lon":6.0833,"lat":50.7833}}
            ]
            """.trimIndent()

            val inputStream = ByteArrayInputStream(jsonString.toByteArray())
            val mockAssets = mockk<android.content.res.AssetManager>()
            every { context.assets } returns mockAssets
            every { mockAssets.open("json_sorted_min.json") } returns inputStream

            // When
            val result = dataSource.getLocalCities()

            // Then
            assertEquals(5, result.size)
            assertEquals("'t Zand", result[0].name)
            assertEquals("'t Hoeksken", result[1].name)
            assertEquals("665 Site Colonia", result[2].name)
            assertEquals("A Coruna", result[3].name)
            assertEquals("Aachen", result[4].name)
        }
}
