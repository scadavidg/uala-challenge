package com.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.data.local.AppSettingsDataSource
import com.data.local.AppSettingsDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    private const val DATASTORE_NAME = "city_prefs"

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> = PreferenceDataStoreFactory.create {
        context.preferencesDataStoreFile(DATASTORE_NAME)
    }

    @Provides
    @Singleton
    fun provideAppSettingsDataSource(dataStore: DataStore<Preferences>): AppSettingsDataSource = AppSettingsDataSourceImpl(dataStore)
}
