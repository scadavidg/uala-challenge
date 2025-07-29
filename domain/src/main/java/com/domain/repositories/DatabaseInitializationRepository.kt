package com.domain.repositories

import com.domain.models.Result

interface DatabaseInitializationRepository {
    suspend fun initializeDatabase(): Result<Boolean>
}
