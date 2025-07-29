package com.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.data.local.CityLocalDataSource
import com.data.local.CityLocalDataSourceImpl
import com.data.local.CityRoomDataSource
import com.data.local.CityRoomDataSourceImpl
import com.data.local.FavoriteCityRoomDataSource
import com.data.local.database.AppDatabase
import com.data.local.migration.JsonToRoomMigrationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalDataSourceModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideCityDao(database: AppDatabase) = database.cityDao()

    @Provides
    @Singleton
    fun provideFavoriteCityDao(database: AppDatabase) = database.favoriteCityDao()

    @Provides
    @Singleton
    fun provideCityLocalDataSource(
        dataStore: DataStore<Preferences>,
        @ApplicationContext context: Context
    ): CityLocalDataSource {
        return CityLocalDataSourceImpl(dataStore, context)
    }

    @Provides
    @Singleton
    fun provideCityRoomDataSource(
        cityDao: com.data.local.dao.CityDao,
        favoriteCityRoomDataSource: FavoriteCityRoomDataSource
    ): CityRoomDataSource {
        return CityRoomDataSourceImpl(cityDao, favoriteCityRoomDataSource)
    }

    @Provides
    @Singleton
    fun provideFavoriteCityRoomDataSource(
        favoriteCityDao: com.data.local.dao.FavoriteCityDao
    ): FavoriteCityRoomDataSource {
        return FavoriteCityRoomDataSource(favoriteCityDao)
    }

    @Provides
    @Singleton
    fun provideJsonToRoomMigrationService(
        @ApplicationContext context: Context,
        database: AppDatabase
    ): JsonToRoomMigrationService {
        return JsonToRoomMigrationService(context, database)
    }
}
