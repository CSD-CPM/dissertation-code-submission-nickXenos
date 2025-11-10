package com.example.parkfinder

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ParkingSpot::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun parkingSpotDao(): ParkingSpotDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "parkfinder-db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
