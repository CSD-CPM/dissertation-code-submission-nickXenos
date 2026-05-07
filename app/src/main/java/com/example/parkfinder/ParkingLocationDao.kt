package com.example.parkfinder

import androidx.room.*

@Dao
interface ParkingLocationDao {

    @Query("SELECT * FROM ParkingLocation")
    suspend fun getAllLocations(): List<ParkingLocation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: ParkingLocation): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(locations: List<ParkingLocation>)
}