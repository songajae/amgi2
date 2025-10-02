package com.songajae.amgi.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [OwnedPackEntity::class],
    version = 1,
    exportSchema = false
)
abstract class OwnedPackDatabase : RoomDatabase() {
    abstract fun ownedPackDao(): OwnedPackDao

    companion object {
        @Volatile
        private var instance: OwnedPackDatabase? = null

        fun getInstance(context: Context): OwnedPackDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    OwnedPackDatabase::class.java,
                    "owned_packs.db"
                ).build().also { instance = it }
            }
        }
    }
}