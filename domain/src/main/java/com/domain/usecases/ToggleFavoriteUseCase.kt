package com.domain.usecases

import com.domain.repositories.CityRepository

class ToggleFavoriteUseCase(
    private val repository: CityRepository
) {
    suspend operator fun invoke(cityId: Int) {
        repository.toggleFavorite(cityId)
    }
}