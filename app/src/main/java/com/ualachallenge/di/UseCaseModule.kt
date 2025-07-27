package com.ualachallenge.di

import com.domain.repositories.CityRepository
import com.domain.usecases.GetCityByIdUseCase
import com.domain.usecases.GetFavoriteCitiesUseCase
import com.domain.usecases.LoadAllCitiesUseCase
import com.domain.usecases.SearchCitiesUseCase
import com.domain.usecases.ToggleFavoriteUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideLoadAllCitiesUseCase(
        repository: CityRepository
    ) = LoadAllCitiesUseCase(repository)

    @Provides
    fun provideSearchCitiesUseCase(
        repository: CityRepository
    ) = SearchCitiesUseCase(repository)

    @Provides
    fun provideToggleFavoriteUseCase(
        repository: CityRepository
    ) = ToggleFavoriteUseCase(repository)

    @Provides
    fun provideGetFavoriteCitiesUseCase(
        repository: CityRepository
    ) = GetFavoriteCitiesUseCase(repository)

    @Provides
    fun provideGetCityByIdUseCase(
        repository: CityRepository
    ) = GetCityByIdUseCase(repository)
}