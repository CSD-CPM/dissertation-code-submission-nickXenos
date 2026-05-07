package com.example.parkfinder

import androidx.room.*

@Dao
interface ParkingSpaceDao {

    @Query("SELECT * FROM ParkingSpace WHERE locationId = :locationId")
    suspend fun getSpacesForLocation(locationId: Int): List<ParkingSpace>

    @Query("SELECT COUNT(*) FROM ParkingSpace WHERE locationId = :locationId AND isAvailable = 1")
    suspend fun getAvailableSpaces(locationId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpaces(spaces: List<ParkingSpace>)

    @Update
    suspend fun updateSpace(space: ParkingSpace)
}