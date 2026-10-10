package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ShortsEntity::class], version = 1, exportSchema = false)
abstract class ShortsDatabase : RoomDatabase() {
    abstract fun shortsDao(): ShortsDao

    companion object {
        @Volatile
        private var INSTANCE: ShortsDatabase? = null

        fun getInstance(context: Context): ShortsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShortsDatabase::class.java,
                    "shorts_creator.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
