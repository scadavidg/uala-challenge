package com.domain.usecases

import com.domain.models.Result
import com.domain.repositories.CityRepository

class ToggleFavoriteUseCase(
    private val repository: CityRepository
) {
    suspend operator fun invoke(cityId: Int): Result<Unit> {
        return repository.toggleFavorite(cityId)
    }
}