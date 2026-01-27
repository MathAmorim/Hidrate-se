package com.example.base.data.model

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconResId: Int,
    val isUnlocked: Boolean = false,
    val progress: Int = 0,
    val maxProgress: Int = 1
)
