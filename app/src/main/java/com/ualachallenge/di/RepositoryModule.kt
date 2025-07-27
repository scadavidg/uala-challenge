package com.ualachallenge.di

import com.data.repositories.CityRepositoryImpl
import com.domain.repositories.CityRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindCityRepository(
        impl: CityRepositoryImpl
    ): CityRepository
}