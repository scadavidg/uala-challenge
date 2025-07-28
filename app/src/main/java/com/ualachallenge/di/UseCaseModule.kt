package com.ualachallenge.di

import com.domain.repositories.CityRepository
import com.domain.usecases.LoadAllCitiesUseCase
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
}
