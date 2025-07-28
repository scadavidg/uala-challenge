package com.data.di

import com.data.remote.CityRemoteDataSource
import com.data.remote.CityRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteDataSourceModule {

    @Binds
    @Singleton
    abstract fun bindCityRemoteDataSource(
        impl: CityRemoteDataSourceImpl
    ): CityRemoteDataSource
}
