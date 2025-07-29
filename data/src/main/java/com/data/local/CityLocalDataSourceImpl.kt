package com.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.data.dto.CityRemoteDto
import com.domain.models.City
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
        val FAVORITES_DATA_KEY = stringSetPreferencesKey("favorite_cities_data")
        private const val CITIES_JSON_FILE = "json_sorted_min"
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

    // New method to remove complete favorite city information
    override suspend fun removeFavoriteCity(cityId: Int) {
        dataStore.edit { prefs ->
            // Remove the ID
            val currentIds = prefs[FAVORITES_KEY]?.toMutableSet() ?: mutableSetOf()
            currentIds.remove(cityId.toString())
            prefs[FAVORITES_KEY] = currentIds

            // Remove complete city data
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
            val adapter = moshi.adapter(City::class.java)

            val currentData = prefs[FAVORITES_DATA_KEY]?.toMutableSet() ?: mutableSetOf()
            val updatedData = currentData.filter { json ->
                try {
                    val city = adapter.fromJson(json)
                    city?.id != cityId
                } catch (e: Exception) {
                    true // Mantener si no se puede parsear
                }
            }.toMutableSet()
            prefs[FAVORITES_DATA_KEY] = updatedData
        }
    }

    // New method to save complete favorite city information
    override suspend fun addFavoriteCity(city: City) {
        dataStore.edit { prefs ->
            // Save the ID
            val currentIds = prefs[FAVORITES_KEY]?.toMutableSet() ?: mutableSetOf()
            currentIds.add(city.id.toString())
            prefs[FAVORITES_KEY] = currentIds

            // Save complete city data
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
            val cityJson = moshi.adapter(City::class.java).toJson(city)

            val currentData = prefs[FAVORITES_DATA_KEY]?.toMutableSet() ?: mutableSetOf()
            currentData.add(cityJson)
            prefs[FAVORITES_DATA_KEY] = currentData
        }
    }

    // New method to get all favorite cities with complete data
    override suspend fun getFavoriteCitiesData(): List<City> {
        val prefs = dataStore.data.first()
        val citiesJson = prefs[FAVORITES_DATA_KEY] ?: emptySet()

        return try {
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
            val adapter = moshi.adapter(City::class.java)

            citiesJson.mapNotNull { json ->
                adapter.fromJson(json)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getLocalCities(): List<CityRemoteDto> = try {
        val jsonString = context.assets.open("json_sorted_min.json").bufferedReader().use { it.readText() }
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
