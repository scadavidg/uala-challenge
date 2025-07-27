package com.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "city_prefs")

class CityLocalDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : CityLocalDataSource {

    private val dataStore = context.dataStore

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

