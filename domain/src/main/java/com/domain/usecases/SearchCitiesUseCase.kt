package com.domain.usecases

import com.domain.models.City
import com.domain.repositories.CityRepository

class SearchCitiesUseCase(
    private val repository: CityRepository
) {
    suspend operator fun invoke(
        prefix: String,
        onlyFavorites: Boolean = false
    ): List<City> {
        return repository.searchCities(prefix, onlyFavorites)
    }
}