package com.domain.usecases

import com.domain.models.City
import com.domain.models.Result
import com.domain.repositories.CityRepository

class GetCityByIdUseCase(
    private val repository: CityRepository
) {
    suspend operator fun invoke(cityId: Int): Result<City?> {
        return repository.getCityById(cityId)
    }
}
