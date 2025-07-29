package com.data.local

interface AppSettingsDataSource {
    suspend fun isOnlineMode(): Boolean
    suspend fun setOnlineMode(enabled: Boolean)
}
