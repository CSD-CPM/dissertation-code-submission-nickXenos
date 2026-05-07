package com.example.parkfinder

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ParkingLocation::class,
        ParkingSpace::class,
        Booking::class
    ],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun locationDao(): ParkingLocationDao
    abstract fun spaceDao(): ParkingSpaceDao
    abstract fun bookingDao(): BookingDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "parkfinder-db"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}