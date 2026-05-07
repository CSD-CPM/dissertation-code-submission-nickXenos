package com.example.parkfinder

import androidx.room.*

@Dao
interface BookingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking)

    @Query("SELECT * FROM Booking WHERE locationId = :locationId")
    suspend fun getBookingsForLocation(locationId: Int): List<Booking>
}