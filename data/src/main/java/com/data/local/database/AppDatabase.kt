package com.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.data.local.dao.CityDao
import com.data.local.dao.FavoriteCityDao
import com.data.local.entity.CityEntity
import com.data.local.entity.FavoriteCityEntity

@Database(
    entities = [CityEntity::class, FavoriteCityEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun cityDao(): CityDao
    abstract fun favoriteCityDao(): FavoriteCityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .setJournalMode(RoomDatabase.JournalMode.TRUNCATE) // Faster for bulk operations
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
