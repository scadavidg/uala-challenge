package com.domain.usecases

import com.domain.models.Result
import com.domain.repositories.DatabaseInitializationRepository

class InitializeRoomDatabaseUseCase(
    private val databaseInitializationRepository: DatabaseInitializationRepository
) {
    suspend operator fun invoke(): Result<Boolean> = databaseInitializationRepository.initializeDatabase()
}
