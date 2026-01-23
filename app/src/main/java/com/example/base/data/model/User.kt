package com.example.base.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val weight: Float,
    val dailyGoal: Int,
    val birthDate: Long = 0L,
    val wakeUpTime: String = "08:00",
    val sleepTime: String = "22:00",
    val onboardingCompleted: Boolean = false
)
