package com.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class AppSettingsDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : AppSettingsDataSource {

    companion object {
        val ONLINE_MODE_KEY = booleanPreferencesKey("online_mode")
    }

    override suspend fun isOnlineMode(): Boolean {
        val prefs = dataStore.data.first()
        return prefs[ONLINE_MODE_KEY] ?: false // Default to offline mode
    }

    override suspend fun setOnlineMode(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[ONLINE_MODE_KEY] = enabled
        }
    }
}
