package com.domain.usecases

import com.domain.models.City
import com.domain.models.Result
import com.domain.repositories.CityRepository

class LoadAllCitiesUseCase(private val repository: CityRepository) {
    suspend operator fun invoke(page: Int = 1, limit: Int = 20): Result<List<City>> = repository.getAllCities(page = page, limit = limit)
}
