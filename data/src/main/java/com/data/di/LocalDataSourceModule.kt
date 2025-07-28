package com.data.di

import com.data.local.CityLocalDataSource
import com.data.local.CityLocalDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalDataSourceModule {

    @Binds
    @Singleton
    abstract fun bindCityLocalDataSource(
        impl: CityLocalDataSourceImpl
    ): CityLocalDataSource
}
