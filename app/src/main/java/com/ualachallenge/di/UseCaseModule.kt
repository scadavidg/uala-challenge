package com.ualachallenge.di

import com.domain.repositories.CityRepository
import com.domain.repositories.DatabaseInitializationRepository
import com.domain.usecases.GetCityByIdUseCase
import com.domain.usecases.GetFavoriteCitiesUseCase
import com.domain.usecases.GetOnlineModeUseCase
import com.domain.usecases.InitializeRoomDatabaseUseCase
import com.domain.usecases.LoadAllCitiesUseCase
import com.domain.usecases.SearchCitiesUseCase
import com.domain.usecases.ToggleFavoriteUseCase
import com.domain.usecases.ToggleOnlineModeUseCase
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
    fun provideToggleFavoriteUseCase(repository: CityRepository): ToggleFavoriteUseCase = ToggleFavoriteUseCase(repository)

    @Provides
    @Singleton
    fun provideGetFavoriteCitiesUseCase(repository: CityRepository): GetFavoriteCitiesUseCase = GetFavoriteCitiesUseCase(repository)

    @Provides
    @Singleton
    fun provideSearchCitiesUseCase(repository: CityRepository): SearchCitiesUseCase = SearchCitiesUseCase(repository)

    @Provides
    @Singleton
    fun provideGetCityByIdUseCase(repository: CityRepository): GetCityByIdUseCase = GetCityByIdUseCase(repository)

    @Provides
    @Singleton
    fun provideToggleOnlineModeUseCase(repository: CityRepository): ToggleOnlineModeUseCase = ToggleOnlineModeUseCase(repository)

    @Provides
    @Singleton
    fun provideGetOnlineModeUseCase(repository: CityRepository): GetOnlineModeUseCase = GetOnlineModeUseCase(repository)

    @Provides
    @Singleton
    fun provideInitializeRoomDatabaseUseCase(
        databaseInitializationRepository: DatabaseInitializationRepository
    ): InitializeRoomDatabaseUseCase = InitializeRoomDatabaseUseCase(databaseInitializationRepository)
}
