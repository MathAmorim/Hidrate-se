package com.example.base.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_record_table")
data class WaterRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Int,
    val date: String,
    val timestamp: Long
)
