package com.example.parkfinder

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ParkingLocation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val totalSpaces: Int
)