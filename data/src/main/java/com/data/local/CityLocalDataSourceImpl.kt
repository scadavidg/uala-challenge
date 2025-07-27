package com.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.first
import javax.inject.Inject


class CityLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : CityLocalDataSource {

    companion object {
        val FAVORITES_KEY = stringSetPreferencesKey("favorite_city_ids")
    }

    override suspend fun getFavoriteIds(): Set<Int> {
        val prefs = dataStore.data.first()
        return prefs[FAVORITES_KEY]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
    }

    override suspend fun isFavorite(cityId: Int): Boolean {
        return getFavoriteIds().contains(cityId)
    }

    override suspend fun addFavorite(cityId: Int) {
        dataStore.edit { prefs ->
            val current = prefs[FAVORITES_KEY]?.toMutableSet() ?: mutableSetOf()
            current.add(cityId.toString())
            prefs[FAVORITES_KEY] = current
        }
    }

    override suspend fun removeFavorite(cityId: Int) {
        dataStore.edit { prefs ->
            val current = prefs[FAVORITES_KEY]?.toMutableSet() ?: mutableSetOf()
            current.remove(cityId.toString())
            prefs[FAVORITES_KEY] = current
        }
    }
}


