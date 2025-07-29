package com.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.data.dto.CityRemoteDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class CityLocalDataSourceImpl @Inject constructor(private val dataStore: DataStore<Preferences>, @ApplicationContext private val context: Context) :
    CityLocalDataSource {

    companion object {
        val FAVORITES_KEY = stringSetPreferencesKey("favorite_city_ids")
        private const val CITIES_JSON_FILE = "json_sorted"
    }

    override suspend fun getFavoriteIds(): Set<Int> {
        val prefs = dataStore.data.first()
        return prefs[FAVORITES_KEY]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
    }

    override suspend fun isFavorite(cityId: Int): Boolean = getFavoriteIds().contains(cityId)

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

    override suspend fun getLocalCities(): List<CityRemoteDto> = try {
        val jsonString = context.assets.open("json_sorted.json").bufferedReader().use { it.readText() }
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val type = Types.newParameterizedType(List::class.java, CityRemoteDto::class.java)
        val adapter = moshi.adapter<List<CityRemoteDto>>(type)
        adapter.fromJson(jsonString) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}
