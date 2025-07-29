package com.domain.usecases

import com.domain.models.Result
import com.domain.repositories.CityRepository

class GetOnlineModeUseCase(private val repository: CityRepository) {
    suspend operator fun invoke(): Result<Boolean> = repository.isOnlineMode()
}
