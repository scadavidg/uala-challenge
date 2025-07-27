package com.domain.usecases

import com.domain.models.City
import com.domain.models.Result
import com.domain.repositories.CityRepository

class GetFavoriteCitiesUseCase(
    private val repository: CityRepository
) {
    suspend operator fun invoke(): Result<List<City>> {
        return repository.getFavoriteCities()
    }
}