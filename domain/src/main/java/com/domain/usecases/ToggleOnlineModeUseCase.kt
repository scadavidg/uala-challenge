package com.domain.usecases

import com.domain.models.Result
import com.domain.repositories.CityRepository

class ToggleOnlineModeUseCase(private val repository: CityRepository) {
    suspend operator fun invoke(enabled: Boolean): Result<Unit> = repository.toggleOnlineMode(enabled)
}
