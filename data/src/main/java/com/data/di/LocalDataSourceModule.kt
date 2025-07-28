package com.data.di

import com.data.local.CityLocalDataSource
import com.data.local.CityLocalDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalDataSourceModule {

    @Binds
    abstract fun bindCityLocalDataSource(
        impl: CityLocalDataSourceImpl
    ): CityLocalDataSource
}
