package com.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AppSettingsDataSourceImplTest {

    private lateinit var dataSource: AppSettingsDataSourceImpl
    private lateinit var mockDataStore: DataStore<Preferences>
    private lateinit var mockPreferences: Preferences

    @BeforeEach
    fun setup() {
        mockDataStore = mockk()
        mockPreferences = mockk()
        dataSource = AppSettingsDataSourceImpl(mockDataStore)
    }

    @Test
    fun `Given online mode is enabled in preferences, When isOnlineMode is called, Then should return true`() = runTest {
        // Given
        coEvery { mockDataStore.data } returns flowOf(mockPreferences)
        coEvery { mockPreferences[AppSettingsDataSourceImpl.ONLINE_MODE_KEY] } returns true

        // When
        val result = dataSource.isOnlineMode()

        // Then
        assertTrue(result)
    }

    @Test
    fun `Given online mode is disabled in preferences, When isOnlineMode is called, Then should return false`() = runTest {
        // Given
        coEvery { mockDataStore.data } returns flowOf(mockPreferences)
        coEvery { mockPreferences[AppSettingsDataSourceImpl.ONLINE_MODE_KEY] } returns false

        // When
        val result = dataSource.isOnlineMode()

        // Then
        assertFalse(result)
    }

    @Test
    fun `Given online mode is not set in preferences, When isOnlineMode is called, Then should return false as default`() = runTest {
        // Given
        coEvery { mockDataStore.data } returns flowOf(mockPreferences)
        coEvery { mockPreferences[AppSettingsDataSourceImpl.ONLINE_MODE_KEY] } returns null

        // When
        val result = dataSource.isOnlineMode()

        // Then
        assertFalse(result)
    }
}
