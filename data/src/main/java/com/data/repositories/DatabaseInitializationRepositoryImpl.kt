package com.data.repositories

import com.data.local.migration.JsonToRoomMigrationService
import com.domain.models.Result
import com.domain.repositories.DatabaseInitializationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseInitializationRepositoryImpl @Inject constructor(
    private val migrationService: JsonToRoomMigrationService
) : DatabaseInitializationRepository {

    override suspend fun initializeDatabase(): Result<Boolean> = try {
        val success = migrationService.migrateIfNeeded()
        if (success) {
            Result.Success(true)
        } else {
            Result.Error("Failed to initialize Room database")
        }
    } catch (e: Exception) {
        Result.Error("Error initializing Room database: ${e.message}")
    }
}
