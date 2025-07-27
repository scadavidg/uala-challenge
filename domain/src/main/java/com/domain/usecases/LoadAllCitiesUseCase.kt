package com.domain.usecases

import com.domain.models.City
import com.domain.repositories.CityRepository

class LoadAllCitiesUseCase(
    private val repository: CityRepository
) {
    suspend operator fun invoke(): List<City> {
        return repository.getAllCities()
    }
}