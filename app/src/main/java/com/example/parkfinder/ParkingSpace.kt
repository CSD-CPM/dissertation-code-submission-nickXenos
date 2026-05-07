package com.example.parkfinder

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ParkingLocation::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ParkingSpace(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val locationId: Int,
    val isAvailable: Boolean = true
)