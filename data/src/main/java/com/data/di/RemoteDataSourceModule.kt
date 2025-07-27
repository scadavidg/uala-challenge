package com.data.di

import com.data.remote.CityRemoteDataSource
import com.data.remote.CityRemoteDataSourceImpl
import com.data.remote.api.CityApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RemoteDataSourceModule {

    @Provides
    fun provideCityRemoteDataSource(
        api: CityApiService
    ): CityRemoteDataSource = CityRemoteDataSourceImpl(api)
}