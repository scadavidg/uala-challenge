package com.ualachallenge.di

import com.domain.repositories.CityRepository
import com.domain.usecases.GetFavoriteCitiesUseCase
import com.domain.usecases.LoadAllCitiesUseCase
import com.domain.usecases.ToggleFavoriteUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideLoadAllCitiesUseCase(repository: CityRepository): LoadAllCitiesUseCase = LoadAllCitiesUseCase(repository)

    @Provides
    @Singleton
    fun provideToggleFavoriteUseCase(
        repository: CityRepository
    ): ToggleFavoriteUseCase = ToggleFavoriteUseCase(repository)

    @Provides
    @Singleton
    fun provideGetFavoriteCitiesUseCase(
        repository: CityRepository
    ): GetFavoriteCitiesUseCase = GetFavoriteCitiesUseCase(repository)
}
