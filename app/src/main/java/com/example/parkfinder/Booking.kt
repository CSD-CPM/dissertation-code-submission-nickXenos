package com.example.parkfinder

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val locationId: Int,
    val startTime: Long,
    val endTime: Long,
    val spacesBooked: Int
)