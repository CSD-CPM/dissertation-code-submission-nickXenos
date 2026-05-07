package com.example.parkfinder

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ParkingSpot(
    @PrimaryKey val title: String,
    val status: String,
    val distance: String,
    val latitude: Double,
    val longitude: Double,

    val totalSpaces: Int = 4,
    val availableSpaces: Int = 4
)