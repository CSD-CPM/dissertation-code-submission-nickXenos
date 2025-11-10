package com.example.parkfinder

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ParkingSpot(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val status: String,
    val distance: String,
    val latitude: Double,
    val longitude: Double
)
