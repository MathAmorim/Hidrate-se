package com.example.base.util

import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

object DateUtils {
    fun getStartOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getEndOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    fun getDayOfWeek(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEE", Locale("pt", "BR"))
        return sdf.format(timestamp)
    }
    
    fun getDayOfMonth(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd", Locale.getDefault())
        return sdf.format(timestamp)
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        return sdf.format(timestamp)
    }
}
