package com.example.parkfinder

import androidx.room.*

@Dao
interface ParkingSpotDao {
    @Query("SELECT * FROM ParkingSpot")
    suspend fun getAllSpots(): List<ParkingSpot>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(spots: List<ParkingSpot>)

    @Query("DELETE FROM ParkingSpot")
    suspend fun clearAll()

    @Query("SELECT * FROM parkingspot WHERE title = :name LIMIT 1")
    suspend fun getSpotByName(name: String): ParkingSpot?

}
