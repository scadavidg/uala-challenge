package com.data.local.migration

import android.content.Context
import android.util.Log
import com.data.dto.CityRemoteDto
import com.data.local.database.AppDatabase
import com.data.local.entity.CityEntity
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okio.buffer
import okio.source

@Singleton
class JsonToRoomMigrationService @Inject constructor(@ApplicationContext private val context: Context, private val database: AppDatabase) {

    companion object {
        private const val CITIES_JSON_FILE = "json_sorted_min"
        private const val MIGRATION_COMPLETED_KEY = "json_migration_completed"
        private const val BATCH_SIZE = 1000 // Increased batch size for better performance
        private const val ESTIMATED_TOTAL_CITIES = 209557 // Based on previous runs
    }

    private val cityDao = database.cityDao()

    // Progress tracking
    private val _migrationProgress = MutableStateFlow(0f)
    val migrationProgress: StateFlow<Float> = _migrationProgress

    private val _isMigrationInProgress = MutableStateFlow(false)
    val isMigrationInProgress: StateFlow<Boolean> = _isMigrationInProgress

    // Methods to control progress from ViewModel
    fun setProgress(progress: Float) {
        _migrationProgress.value = progress
    }

    fun setMigrationInProgress(inProgress: Boolean) {
        _isMigrationInProgress.value = inProgress
    }

    suspend fun migrateIfNeeded(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Check if migration is already completed
            if (isMigrationCompleted()) {
                return@withContext true
            }

            // Check if database already has data
            val citiesCount = cityDao.getCitiesCount()
            if (citiesCount > 0) {
                markMigrationAsCompleted()
                return@withContext true
            }

            // Start migration progress tracking
            _isMigrationInProgress.value = true
            _migrationProgress.value = 0f

            // Try optimized approach first, then fallback to others
            Log.d("JsonMigration", "Starting JSON migration...")
            val success = loadCitiesFromJsonOptimized() || loadCitiesFromJsonStreaming() || loadCitiesFromJsonAlternative()
            if (success) {
                Log.d("JsonMigration", "Migration completed successfully")
                markMigrationAsCompleted()
                // Don't set progress to 100% here, let the ViewModel handle it
                return@withContext true
            } else {
                Log.e("JsonMigration", "Migration failed")
                _isMigrationInProgress.value = false
            }

            return@withContext false
        } catch (e: Exception) {
            // Log error but don't crash the app
            e.printStackTrace()
            _isMigrationInProgress.value = false
            return@withContext false
        }
    }

    private suspend fun loadCitiesFromJsonStreaming(): Boolean {
        return try {
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
            val adapter = moshi.adapter(CityRemoteDto::class.java)

            context.assets.open("$CITIES_JSON_FILE.json").use { inputStream ->
                val source = inputStream.source().buffer()
                val reader = JsonReader.of(source)

                val batch = mutableListOf<CityEntity>()
                var processedCities = 0
                var errorCount = 0
                val maxErrors = 100 // Allow some parsing errors but not too many

                // Start reading the JSON array
                reader.beginArray()
                Log.d("JsonMigration", "Starting streaming migration...")

                while (reader.hasNext()) {
                    try {
                        val cityDto = adapter.fromJson(reader)

                        cityDto?.let { dto ->
                            val entity = CityEntity(
                                id = dto._id,
                                name = dto.name,
                                country = dto.country,
                                lat = dto.coordinates.lat,
                                lon = dto.coordinates.lon
                            )
                            batch.add(entity)
                        }
                    } catch (e: Exception) {
                        errorCount++
                        Log.w("JsonMigration", "Error parsing city at position $processedCities: ${e.message}")
                        if (errorCount > maxErrors) {
                            Log.e("JsonMigration", "Too many parsing errors, aborting migration")
                            return false
                        }
                        // Skip this item and continue
                        continue
                    }

                    // Insert batch when it reaches the batch size
                    if (batch.size >= BATCH_SIZE) {
                        try {
                            // Use smaller transactions to avoid blocking the database
                            database.runInTransaction {
                                cityDao.insertCitiesSync(batch)
                            }
                            processedCities += batch.size
                            Log.d("JsonMigration", "Processed $processedCities cities so far...")
                            batch.clear()

                            // Update progress
                            val progress = (processedCities.toFloat() / ESTIMATED_TOTAL_CITIES * 90).coerceAtMost(90f)
                            _migrationProgress.value = progress
                        } catch (e: Exception) {
                            Log.e("JsonMigration", "Error inserting batch: ${e.message}")
                            return false
                        }
                    }
                }

                reader.endArray()

                // Insert remaining cities
                if (batch.isNotEmpty()) {
                    try {
                        database.runInTransaction {
                            cityDao.insertCitiesSync(batch)
                        }
                        processedCities += batch.size
                        Log.d("JsonMigration", "Final batch inserted. Total processed: $processedCities")
                    } catch (e: Exception) {
                        Log.e("JsonMigration", "Error inserting final batch: ${e.message}")
                        return false
                    }
                }
            }
            Log.d("JsonMigration", "Streaming migration completed successfully")
            true
        } catch (e: Exception) {
            Log.e("JsonMigration", "Streaming migration failed", e)
            e.printStackTrace()
            false
        }
    }

    private suspend fun loadCitiesFromJsonAlternative(): Boolean {
        return try {
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
            val adapter = moshi.adapter(CityRemoteDto::class.java)

            context.assets.open("$CITIES_JSON_FILE.json").use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))

                // Read the opening bracket
                var line = reader.readLine()
                if (line?.trim() != "[") {
                    Log.e("JsonMigration", "Invalid JSON format: missing opening bracket")
                    return false
                }

                val batch = mutableListOf<CityEntity>()
                var processedCities = 0
                var errorCount = 0
                val maxErrors = 100
                Log.d("JsonMigration", "Starting alternative migration...")

                while (true) {
                    line = reader.readLine()?.trim()
                    if (line == null || line == "]") break

                    // Skip empty lines
                    if (line.isEmpty()) continue

                    // Remove trailing comma if present
                    if (line.endsWith(",")) {
                        line = line.substring(0, line.length - 1)
                    }

                    // Skip if it's just a comma
                    if (line == ",") continue

                    try {
                        val cityDto = adapter.fromJson(line)
                        cityDto?.let { dto ->
                            val entity = CityEntity(
                                id = dto._id,
                                name = dto.name,
                                country = dto.country,
                                lat = dto.coordinates.lat,
                                lon = dto.coordinates.lon
                            )
                            batch.add(entity)
                        }
                    } catch (e: Exception) {
                        errorCount++
                        Log.w("JsonMigration", "Alternative method: Error parsing city: ${e.message}")
                        if (errorCount > maxErrors) {
                            Log.e("JsonMigration", "Alternative method: Too many parsing errors, aborting migration")
                            return false
                        }
                        // Skip malformed JSON objects
                        continue
                    }

                    // Insert batch when it reaches the batch size
                    if (batch.size >= BATCH_SIZE) {
                        try {
                            cityDao.insertCities(batch)
                            processedCities += batch.size
                            Log.d("JsonMigration", "Alternative method: Processed $processedCities cities so far...")
                            batch.clear()
                        } catch (e: Exception) {
                            Log.e("JsonMigration", "Alternative method: Error inserting batch: ${e.message}")
                            return false
                        }
                    }
                }

                // Insert remaining cities
                if (batch.isNotEmpty()) {
                    try {
                        cityDao.insertCities(batch)
                        processedCities += batch.size
                        Log.d("JsonMigration", "Alternative method: Final batch inserted. Total processed: $processedCities")
                    } catch (e: Exception) {
                        Log.e("JsonMigration", "Alternative method: Error inserting final batch: ${e.message}")
                        return false
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e("JsonMigration", "Alternative migration failed", e)
            e.printStackTrace()
            false
        }
    }

    private suspend fun loadCitiesFromJsonOptimized(): Boolean {
        return try {
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
            val adapter = moshi.adapter(CityRemoteDto::class.java)

            context.assets.open("$CITIES_JSON_FILE.json").use { inputStream ->
                val source = inputStream.source().buffer()
                val reader = JsonReader.of(source)

                val allCities = mutableListOf<CityEntity>()
                var processedCities = 0
                var errorCount = 0
                val maxErrors = 100

                // Start reading the JSON array
                reader.beginArray()
                Log.d("JsonMigration", "Starting optimized migration...")

                while (reader.hasNext()) {
                    try {
                        val cityDto = adapter.fromJson(reader)

                        cityDto?.let { dto ->
                            val entity = CityEntity(
                                id = dto._id,
                                name = dto.name,
                                country = dto.country,
                                lat = dto.coordinates.lat,
                                lon = dto.coordinates.lon
                            )
                            allCities.add(entity)
                            processedCities++

                            if (processedCities % 2000 == 0) {
                                Log.d("JsonMigration", "Parsed $processedCities cities...")
                                // Report parsing progress (40% of total work)
                                val parsingProgress = (processedCities.toFloat() / ESTIMATED_TOTAL_CITIES) * 40f
                                _migrationProgress.value = parsingProgress
                            }
                        }
                    } catch (e: Exception) {
                        errorCount++
                        if (errorCount > maxErrors) {
                            Log.e("JsonMigration", "Too many parsing errors, aborting migration")
                            return false
                        }
                        continue
                    }
                }

                reader.endArray()
                Log.d("JsonMigration", "Parsing completed. Inserting $processedCities cities...")

                // Report 40% progress after parsing
                _migrationProgress.value = 40f

                // Insert all cities in batches with progress updates
                val batchSize = 2000
                val totalBatches = (allCities.size + batchSize - 1) / batchSize
                var currentBatch = 0

                allCities.chunked(batchSize).forEach { batch ->
                    database.runInTransaction {
                        cityDao.insertCitiesSync(batch)
                    }
                    currentBatch++

                    // Report insertion progress (40% to 90%)
                    val insertionProgress = 40f + (currentBatch.toFloat() / totalBatches) * 50f
                    _migrationProgress.value = insertionProgress

                    Log.d("JsonMigration", "Inserted batch $currentBatch/$totalBatches")
                }

                // Report 90% progress after insertion
                _migrationProgress.value = 90f

                // Small delay to show the progress
                kotlinx.coroutines.delay(200)

                Log.d("JsonMigration", "Optimized migration completed successfully with $processedCities cities")
            }
            true
        } catch (e: Exception) {
            Log.e("JsonMigration", "Optimized migration failed", e)
            e.printStackTrace()
            false
        }
    }

    private suspend fun loadCitiesFromJson(): List<CityRemoteDto> = try {
        val jsonString = context.assets.open("$CITIES_JSON_FILE.json").bufferedReader().use { it.readText() }
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val type = Types.newParameterizedType(List::class.java, CityRemoteDto::class.java)
        val adapter = moshi.adapter<List<CityRemoteDto>>(type)
        adapter.fromJson(jsonString) ?: emptyList()
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }

    private fun isMigrationCompleted(): Boolean {
        val sharedPrefs = context.getSharedPreferences("migration_prefs", Context.MODE_PRIVATE)
        return sharedPrefs.getBoolean(MIGRATION_COMPLETED_KEY, false)
    }

    private fun markMigrationAsCompleted() {
        val sharedPrefs = context.getSharedPreferences("migration_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean(MIGRATION_COMPLETED_KEY, true).apply()
    }
}
