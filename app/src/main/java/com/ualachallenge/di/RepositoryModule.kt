package com.ualachallenge.di

import com.data.repositories.CityRepositoryImpl
import com.data.repositories.DatabaseInitializationRepositoryImpl
import com.domain.repositories.CityRepository
import com.domain.repositories.DatabaseInitializationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCityRepository(
        cityRepositoryImpl: CityRepositoryImpl
    ): CityRepository

    @Binds
    @Singleton
    abstract fun bindDatabaseInitializationRepository(
        databaseInitializationRepositoryImpl: DatabaseInitializationRepositoryImpl
    ): DatabaseInitializationRepository
}
